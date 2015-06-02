//Java Launcher Linux

// version 1.2
// supports passing command line options to java main()
// now supports Linux

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <ctype.h>
#include <stddef.h>

#include <pthread.h>
#include <unistd.h>
#include <dlfcn.h>

#ifndef MAX_PATH
  #define MAX_PATH 255
#endif

#include <jfjava.h>

/* Global variables */
int type;
char version[MAX_PATH];
char javahome[MAX_PATH];
char dll[MAX_PATH];
int size = MAX_PATH;
int (*CreateJavaVM)(void*,void*,void*);
int thread_handle;
int thread_id;
char **g_argv;
int g_argc;
void *jvm_dll;
void *jawt_dll;
pthread_t thread;
pthread_attr_t thread_attr;
char link1[MAX_PATH];
char link2[MAX_PATH];

/* Prototypes */
void error(char *msg);
int JavaThread(void *ignore);

/** Displays the error message in a dialog box. */
void error(char *msg) {
  printf("Failed to start Java\nPlease visit www.java.com and install Java\nError:%s\n", msg);
  exit(0);
}

/** Converts array of C strings into array of Java strings */
jobjectArray
ConvertStringArray(JNIEnv *env, char **strv, int strc)
{
  jarray cls;
  jarray ary;
  jstring str;
  int i;

  cls = (*env)->FindClass(env, "java/lang/String");
/*
  if (cls == NULL) {
    error("Unable to load java/lang/String class");
    return NULL;
  }
*/
  ary = (*env)->NewObjectArray(env, strc, cls, 0);
  for (i = 0; i < strc; i++) {
    str = (*env)->NewStringUTF(env, *strv++);
    (*env)->SetObjectArrayElement(env, ary, i, str);
    (*env)->DeleteLocalRef(env, str);
  }
  return ary;
}

char *DOption = "-Djava.class.path=";

/** Create class path adding /usr/share/java to each element, and change ; to : */
char *CreateClassPath() {
  char *ClassPath;
  int sl = strlen(CLASSPATH);
  ClassPath = malloc(sl + 1);
  strcpy(ClassPath, CLASSPATH);
  int ml = strlen("/usr/share/java/");
  char *jar[32];
  jar[0] = ClassPath;
  int cnt = 1;
  int a;
  for(a=0;a<sl;a++) {
    if (ClassPath[a] == ';') {
      jar[cnt++] = ClassPath + a + 1;
      ClassPath[a] = 0;
    }
  }
  char *ExpandedClassPath = malloc(strlen(DOption) + sl + (ml * cnt) + 1);
  ExpandedClassPath[0] = 0;
  strcat(ExpandedClassPath, DOption);
  for(a=0;a<cnt;a++) {
    if (a > 0) strcat(ExpandedClassPath, ":");
    strcat(ExpandedClassPath, "/usr/share/java/");
    strcat(ExpandedClassPath, jar[a]);
  }
  return ExpandedClassPath;
}

void printException(JNIEnv *env) {
  jthrowable exc;
  exc = (*env)->ExceptionOccurred(env);
  if (exc == NULL) return;
  jclass newExcCls;
  (*env)->ExceptionDescribe(env);
  (*env)->ExceptionClear(env);
}

/** Continues loading the JVM in a new Thread. */
int JavaThread(void *ignore) {
  JavaVM *jvm = NULL;
  JNIEnv *env = NULL;
  JavaVMInitArgs args;
  JavaVMOption options[1];

  memset(&args, 0, sizeof(args));
  args.version = JNI_VERSION_1_2;
  args.nOptions = 1;
  args.options = options;
  args.ignoreUnrecognized = JNI_FALSE;

  options[0].optionString = CreateClassPath();
  options[0].extraInfo = NULL;

  if ((*CreateJavaVM)(&jvm, &env, &args) == -1) {
    error("Unable to create Java VM");
    return -1;
  }

  jclass cls = (*env)->FindClass(env, MAINCLASS);
  if (cls == NULL) {
    printException(env);
    error("Unable to find main class");
    return -1;
  }
  jmethodID mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
  if (mid == NULL) {
    error("Unable to find main method");
    return -1;
  }
  char **argv = g_argv;
  int argc = g_argc;
  //skip argv[0]
  argv++;
  argc--;
  (*env)->CallStaticVoidMethod(env, cls, mid, ConvertStringArray(env, argv, argc));
  (*jvm)->DestroyJavaVM(jvm);  //waits till all threads are complete
  //NOTE : Swing creates the EDT to keep Java alive until all windows are disposed
}

/** Main entry point. */
int main(int argc, char **argv) {
  void *retval;
  g_argv = argv;
  g_argc = argc;

  //get java home
  strcpy(link1, "/usr/bin/java");
  do {
    //with alternatives this can resolve a few times
    int len = readlink(link1, link2, MAX_PATH);
    if (len == -1) break;
    link2[len] = 0;
    strcpy(link1, link2);
  } while (1);
  strcpy(javahome, link1);
  //remove /bin/java from javahome
  char *_java = strrchr(javahome, '/');
  *_java = 0;
  char *_bin = strrchr(javahome, '/');
  *_bin = 0;
  strcat(javahome, "/lib/" ARCH);

  //open libjvm.so
  strcpy(dll, javahome);
  strcat(dll, "/server/libjvm.so");

  jvm_dll = dlopen(dll, RTLD_NOW);
  if (jvm_dll == NULL) {
    error("Unable to open libjvm.so");
  }

  //open libjawt.so (otherwise Linux can't find it later)
  strcpy(dll, javahome);
  strcat(dll, "/libjawt.so");

  jawt_dll = dlopen(dll, RTLD_NOW);
  if (jawt_dll == NULL) {
    error("Unable to open libjawt.so");
  }

  CreateJavaVM = (int (*)(void*,void*,void*)) dlsym(jvm_dll, "JNI_CreateJavaVM");
  if (CreateJavaVM == NULL) {
    error("Unable to find Java interfaces in libjvm.so");
  }

  //now continue in new thread (not really necessary but avoids some Java bugs)
  pthread_attr_init(&thread_attr);

  pthread_create(&thread, &thread_attr, (void *(*) (void *))&JavaThread, NULL);

  pthread_join(thread, &retval);

  return 0;
}
