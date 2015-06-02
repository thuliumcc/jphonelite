//Java Launcher Win32/64

// version 1.1
// supports passing command line options to java main()

#include <windows.h>
#include <io.h>
#include <process.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <ctype.h>
#include <stddef.h>

#include <jni.h>

/* Global variables */
HKEY key, subkey;
int type;
char version[MAX_PATH];
char javahome[MAX_PATH];
char dll[MAX_PATH];
char crt[MAX_PATH];
int size = MAX_PATH;
HMODULE crt_dll;
HMODULE jvm_dll;
int (*CreateJavaVM)(void*,void*,void*);
HANDLE thread_handle;
int thread_id;
STARTUPINFO si;
PROCESS_INFORMATION pi;
int tryOther = 1;
char **g_argv;
int g_argc;
char module[MAX_PATH];

/* Prototypes */
void error(char *msg);
void loadOther();
int JavaThread(void *ignore);

/** Launches the other 32/64bits executable. */
void loadOther() {
  char exe[MAX_PATH];  //c:\program files\app\file32.exe
  char opts[MAX_PATH + 128];
  int a;
  strcpy(exe, module);
  int sl = strlen(exe);
  switch (sizeof(void*)) {
    case 4:
      exe[sl-6] = '6';
      exe[sl-5] = '4';
      break;
    case 8:
      exe[sl-6] = '3';
      exe[sl-5] = '2';
      break;
  }
  memset(&si, 0, sizeof(STARTUPINFO));
  si.cb = sizeof(STARTUPINFO);
  opts[0] = 0;
  strcat(opts, "\"");
  strcat(opts, exe);
  strcat(opts, "\"");
  strcat(opts, " ---otherBits");
  for(a=1;a<g_argc;a++) {
    strcat(opts, " \"");
    strcat(opts, g_argv[a]);
    strcat(opts, "\"");
  }
  tryOther = 0;
  if (!CreateProcess(exe, opts, NULL, NULL, FALSE, NORMAL_PRIORITY_CLASS, NULL, NULL, &si, &pi)) {
    error("Failed to execute other launcher");
  }
}

/** Displays the error message in a dialog box. */
void error(char *msg) {
  char fullmsg[1024];
  sprintf(fullmsg, "Failed to start Java\nPlease visit www.java.com and install Java\nError(%d):%s", sizeof(void*) * 8, msg);
  if (tryOther) {
    loadOther();
  } else {
    MessageBox(NULL, fullmsg, "Java Virtual Machine Launcher", (MB_OK | MB_ICONSTOP | MB_APPLMODAL));
  }
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
int noExpandClassPath = 0;

/** Create class path adding module path to each element. */
char *CreateClassPath() {
  if (noExpandClassPath) {
    return "-Djava.class.path=" CLASSPATH;
  }
  char *ClassPath;
  int sl = strlen(CLASSPATH);
  ClassPath = malloc(sl + 1);
  strcpy(ClassPath, CLASSPATH);
  char *LastPath = strrchr(module, '\\');
  LastPath++;
  char OrgChar = *LastPath;
  *LastPath = 0;
  int ml = strlen(module);
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
    if (a > 0) strcat(ExpandedClassPath, ";");
    strcat(ExpandedClassPath, module);
    strcat(ExpandedClassPath, jar[a]);
  }
  *LastPath = OrgChar;
  return ExpandedClassPath;
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
  if (cls == 0) {
    error("Unable to find main class");
    return -1;
  }
  jmethodID mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
  if (mid == 0) {
    error("Unable to find main member");
    return -1;
  }
  char **argv = g_argv;
  int argc = g_argc;
  //skip argv[0]
  argv++;
  argc--;
  if (g_argc > 1 && stricmp(g_argv[1], "---otherBits") == 0) {
    //skip special passed value
    argv++;
    argc--;
  }
  (*env)->CallStaticVoidMethod(env, cls, mid, ConvertStringArray(env, argv, argc));
  (*jvm)->DestroyJavaVM(jvm);  //waits till all threads are complete (Swing creates the EDT to keep Java alive until all windows are closed)
}

/** Main entry point. */
int main(int argc, char **argv) {
  g_argv = argv;
  g_argc = argc;
  if (argc > 1 && stricmp(argv[1], "---otherBits") == 0) {
    tryOther = 0;
  }

  GetModuleFileName(NULL, module, MAX_PATH);

  if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, "Software\\JavaSoft\\Java Runtime Environment", 0, KEY_READ, &key) != 0) {
    error("Unable to open Java Registry");
    return -1;
  }

  size = 0;
  if (RegQueryValueEx(key, "CurrentVersion", 0, (LPDWORD)&type, 0, (LPDWORD)&size) != 0 || (type != REG_SZ) || (size > MAX_PATH)) {
    error("Unable to open Java Registry");
    return -1;
  }

  size = MAX_PATH;
  if (RegQueryValueEx(key, "CurrentVersion", 0, 0, version, (LPDWORD)&size) != 0) {
    error("Unable to open Java Registry");
    return -1;
  }

  if (RegOpenKeyEx(key, version, 0, KEY_READ, &subkey) != 0) {
    error("Unable to open Java Registry");
    return -1;
  }

  size = 0;
  if (RegQueryValueEx(subkey, "JavaHome", 0, (LPDWORD)&type, 0, (LPDWORD)&size) != 0 || (type != REG_SZ) || (size > MAX_PATH)) {
    error("Unable to open Java Registry");
    return -1;
  }

  size = MAX_PATH;
  if (RegQueryValueEx(subkey, "JavaHome", 0, 0, javahome, (LPDWORD)&size) != 0) {
    error("Unable to open Java Registry");
    return -1;
  }

  RegCloseKey(key);
  RegCloseKey(subkey);

  //JRE7/8
  strcpy(crt, javahome);
  strcat(crt, "\\bin\\msvcr100.dll");
  if ((crt_dll = LoadLibrary(crt)) == 0) {
    //older JRE5/6 version
    strcpy(crt, javahome);
    strcat(crt, "\\bin\\msvcr71.dll");
    if ((crt_dll = LoadLibrary(crt)) == 0) {
      //could be a much older version (JRE5???) which just uses msvcrt.dll
    }
  }

  strcpy(dll, javahome);
  strcat(dll, "\\bin\\server\\jvm.dll");
  if ((jvm_dll = LoadLibrary(dll)) == 0) {
    strcpy(dll, javahome);
    strcat(dll, "\\bin\\client\\jvm.dll");
    if ((jvm_dll = LoadLibrary(dll)) == 0) {
      error("Unable to open jvm.dll");
      return -1;
    }
  }

  //from this point on do not try other bits
  tryOther = 0;

  CreateJavaVM = (int (*)(void*,void*,void*)) GetProcAddress(jvm_dll, "JNI_CreateJavaVM");
  if (CreateJavaVM == 0) {
    error("Unable to find Java interfaces in jvm.dll");
    return -1;
  }

  //now continue in new thread (not really necessary but avoids some Java bugs)
  thread_handle = CreateThread(NULL, 64 * 1024, (LPTHREAD_START_ROUTINE)&JavaThread, NULL, 0, (LPDWORD)&thread_id);

  WaitForSingleObject(thread_handle, INFINITE);

  return 0;
}
