package javaforce.linux;

/**
 * Created : Mar 12, 2012
 *
 * @author pquiring
 */
import java.util.*;
import java.io.*;
import java.awt.*;

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import javaforce.*;

/**
 * Common functions for Linux administration.
 */

public class Linux {

  public static enum DistroTypes {
    Unknown, Ubuntu, Fedora
  };
  public static DistroTypes distro = DistroTypes.Unknown;

  /**
   * Detects Linux distribution type. (Support Ubuntu, Fedora currently)
   */
  public static boolean detectDistro() {
    if (distro != DistroTypes.Unknown) {
      return true;
    }
    try {
      //Ubuntu : /etc/lsb-release
      File lsb = new File("/etc/lsb-release");
      if (lsb.exists()) {
        FileInputStream fis = new FileInputStream(lsb);
        byte data[] = JF.readAll(fis);
        fis.close();
        String str = new String(data);
        if (str.indexOf("Ubuntu") != -1) {
          distro = DistroTypes.Ubuntu;
          return true;
        }
      }
      //Fedora : /etc/fedora-release
      File fedora = new File("/etc/fedora-release");
      if (fedora.exists()) {
        FileInputStream fis = new FileInputStream(fedora);
        byte data[] = JF.readAll(fis);
        fis.close();
        String str = new String(data);
        if (str.indexOf("Fedora") != -1) {
          distro = DistroTypes.Fedora;
          return true;
        }
      }
    } catch (Exception e) {
      JFLog.log(e);
    }
    return false;
  }

  /**
   * Creates folder as root
   */
  public static boolean mkdir(String folder) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("sudo");
    cmd.add("mkdir");
    cmd.add("-p");
    cmd.add(folder);
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Failed to exec mkdir");
      return false;
    }
    if (output.length() > 0) {
      JFLog.log("Error:" + output);
      return false;
    }
    return true;
  }

  /**
   * Copies src to dst as root
   */
  public static boolean copyFile(String src, String dst) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("sudo");
    cmd.add("cp");
    cmd.add(src);
    cmd.add(dst);
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Failed to exec cp");
      return false;
    }
    if (output.length() > 0) {
      JFLog.log("Error:" + output);
      return false;
    }
    return true;
  }

  /**
   * Creates Link to Target as root
   */
  public static boolean createLink(String target, String link) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("sudo");
    cmd.add("ln");
    cmd.add("-s");
    cmd.add(target);
    cmd.add(link);
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Failed to exec ln");
      return false;
    }
    if (output.length() > 0) {
      JFLog.log("Error:" + output);
      return false;
    }
    return true;
  }

  /**
   * Deletes file as root
   */
  public static boolean deleteFile(String file) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("sudo");
    cmd.add("rm");
    cmd.add("-f");
    cmd.add(file);
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Failed to exec ln");
      return false;
    }
    if (output.length() > 0) {
      JFLog.log("Error:" + output);
      return false;
    }
    return true;
  }

  /**
   * Restarts a service
   */
  public static boolean restartService(String name) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("sudo");
    cmd.add("service");
    cmd.add(name);
    cmd.add("restart");
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Failed to exec service");
      return false;
    }
    if (sp.getErrorLevel() != 0) {
      JFLog.log("Error:" + output);
      return false;
    }
    return true;
  }

  /**
   * Restarts a JF service
   */
  public static boolean restartJFService(String name) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("sudo");
    cmd.add("jservice");
    cmd.add(name);
    cmd.add("restart");
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Failed to exec jservice");
      return false;
    }
    if (sp.getErrorLevel() != 0) {
      JFLog.log("Error:" + output);
      return false;
    }
    return true;
  }

  /**
   * Work with Ubuntu packages.
   */
  private static boolean apt(String action, String pkg, String desc) {
    JFTask task = new JFTask() {
      public boolean work() {
        this.setProgress(-1);
        String action = (String)this.getProperty("action");
        String pkg = (String)this.getProperty("pkg");
        String desc = (String)this.getProperty("desc");
        setLabel((action.equals("install") ? "Installing " : "Removing ") + desc);
        ShellProcess sp = new ShellProcess();
        sp.removeEnvironmentVariable("TERM");
        sp.addEnvironmentVariable("DEBIAN_FRONTEND", "noninteractive");
        String output = sp.run(new String[]{"sudo", "-E", "apt-get", "--yes", "--allow-unauthenticated", action, pkg}, true);
        if (output == null) {
          setLabel("Failed to exec apt-get");
          JFLog.log("Failed to exec apt-get");
          return false;
        }
        if (output.indexOf("Unable to locate package") != -1) {
          setLabel("Package not found");
          JFLog.log("Package not found");
          return false;
        }
        setLabel("Complete");
        setProgress(100);
        return true;
      }
    };
    task.setProperty("action", action);
    task.setProperty("pkg", pkg);
    task.setProperty("desc", desc);
    new ProgressDialog(null, true, task).setVisible(true);
    return task.getStatus();
  }

  /**
   * Work with Fedora packages.
   */
  private static boolean yum(String action, String pkg, String desc) {
    JFTask task = new JFTask() {
      public boolean work() {
        this.setProgress(-1);
        String action = (String)this.getProperty("action");
        String pkg = (String)this.getProperty("pkg");
        String desc = (String)this.getProperty("desc");
        setLabel((action.equals("install") ? "Installing " : "Removing ") + desc);
        ShellProcess sp = new ShellProcess();
        sp.removeEnvironmentVariable("TERM");  //prevent config dialogs
        String output = sp.run(new String[]{"sudo", "-E", "openpty", "yum", "-y", action, pkg}, false);
        if (output == null) {
          setLabel("Failed to exec yum");
          JFLog.log("Failed to exec yum");
          return false;
        }
        setLabel("Complete");
        setProgress(100);
        return true;
      }
    };
    task.setProperty("action", action);
    task.setProperty("pkg", pkg);
    task.setProperty("desc", desc);
    new ProgressDialog(null, true, task).setVisible(true);
    return task.getStatus();
  }

  public static boolean installPackage(String pkg, String desc) {
    detectDistro();
    switch (distro) {
      case Ubuntu:
        return apt("install", pkg, desc);
      case Fedora:
        return yum("install", pkg, desc);
    }
    return false;
  }

  public static boolean removePackage(String pkg, String desc) {
    detectDistro();
    switch (distro) {
      case Ubuntu:
        return apt("autoremove", pkg, desc);
      case Fedora:
        return yum("remove", pkg, desc);
    }
    return false;
  }

  /**
   * Sets file as executable as root
   */
  public static boolean setExec(String file) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("sudo");
    cmd.add("chmod");
    cmd.add("+x");
    cmd.add(file);
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Failed to exec chmod");
      return false;
    }
    if (output.length() > 0) {
      JFLog.log("Error:" + output);
      return false;
    }
    return true;
  }
  private static String pkgList[][];

  /*  public static String[][] getPackages() {
   if (dpkg == null) updateInstalled();
   return dpkg;
   }*/
  private static String[][] ubuntu_searchPackages(String regex) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("apt-cache");
    cmd.add("search");
    cmd.add(regex);
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Error:unable to execute apt-cache");
      return null;
    }
    String lns[] = output.split("\n");
    String ret[][] = new String[lns.length][2];
    String f[];
    for (int a = 0; a < lns.length; a++) {
      f = lns[a].split(" - ");
      if (f.length != 2) {
        continue;
      }
      ret[a][0] = f[0];  //package name
      ret[a][1] = f[1];  //package desc
    }
    return ret;
  }

  private static String[][] fedora_searchPackages(String regex) {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("yum");
    cmd.add("search");
    cmd.add(regex);
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Error:unable to execute yum");
      return null;
    }
    String lns[] = output.split("\n");
    String ret[][] = new String[lns.length][2];
    String f[];
    for (int a = 0; a < lns.length; a++) {
      f = lns[a].split(" : ");
      if (f.length != 2) {
        continue;
      }
      ret[a][0] = f[0];  //package name
      ret[a][1] = f[1];  //package desc
    }
    return ret;
  }

  /**
   * Searches for available packages (NOTE:There may be nulls in the output)
   */
  public static String[][] searchPackages(String regex) {
    detectDistro();
    switch (distro) {
      case Ubuntu:
        return ubuntu_searchPackages(regex);
      case Fedora:
        return fedora_searchPackages(regex);
    }
    return null;
  }

  public static void ubuntu_updateInstalled() {
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("dpkg");
    cmd.add("-l");
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Error:unable to execute dpkg");
      return;
    }
    String lns[] = output.split("\n");
    pkgList = new String[lns.length - 5][3];
    String f[];
    for (int a = 5; a < lns.length; a++) {
      f = lns[a].split(" +", 4);  //greedy spaces
      pkgList[a - 5][0] = f[1];  //package name
      pkgList[a - 5][1] = f[3];  //package desc
      pkgList[a - 5][2] = (f[0].charAt(0) == 'i' ? "true" : "false");  //package installed?
    }
  }

  public static void fedora_updateInstalled() {
    //NOTE:can't use "rpm -qa" because the version # is mangled in the name
    ShellProcess sp = new ShellProcess();
    ArrayList<String> cmd = new ArrayList<String>();
    cmd.add("yum");
    cmd.add("list");
    cmd.add("installed");
    String output = sp.run(cmd, false);
    if (output == null) {
      JFLog.log("Error:unable to execute yum");
      return;
    }
    String lns[] = output.split("\n");
    pkgList = new String[lns.length - 2][3];
    for (int a = 2; a < lns.length; a++) {
      String f[] = lns[a].split(" +");  //greedy spaces
      if (f.length != 3) {
        pkgList[a-2][0] = "";
        pkgList[a-2][1] = "";
        pkgList[a-2][2] = "";
        continue;
      }
      int idx = f[0].lastIndexOf(".");  //strip arch
      if (idx == -1) {
        pkgList[a-2][0] = f[0];  //package name
      } else {
        pkgList[a-2][0] = f[0].substring(0, idx);  //package name
      }
      pkgList[a-2][1] = f[1];  //package desc
      pkgList[a-2][2] = "true";  //package installed?
    }
  }

  public static void updateInstalled() {
    detectDistro();
    switch (distro) {
      case Ubuntu:
        ubuntu_updateInstalled();
        break;
      case Fedora:
        fedora_updateInstalled();
        break;
    }
  }

  public static boolean isInstalled(String pkg) {
    if (pkg == null) {
      return true;
    }
    if (pkgList == null) {
      updateInstalled();
    }
    for (int a = 0; a < pkgList.length; a++) {
      if (pkgList[a][0].equals(pkg)) {
        return pkgList[a][2].equals("true");
      }
    }
    return false;
  }

  public static String getPackageDesc(String pkg) {
    if (pkg == null) {
      return "";
    }
    if (pkgList == null) {
      updateInstalled();
      if (pkgList == null) {
        return "";
      }
    }
    for (int a = 0; a < pkgList.length; a++) {
      if (pkgList[a][0].equals(pkg)) {
        return pkgList[a][1];
      }
    }
    return "";
  }

  public static boolean isMemberOf(String user, String group) {
    try {
      ShellProcess sp = new ShellProcess();
      String output = sp.run(new String[]{"groups", user}, false).replaceAll("\n", "");
      //output = "user : group1 group2 ..."
      String groups[] = output.split(" ");
      for (int a = 2; a < groups.length; a++) {
        if (groups[a].equals(group)) {
          return true;
        }
      }
      return false;
    } catch (Exception e) {
      JFLog.log(e);
      return false;
    }
  }

  private static String expandQuotes(String inString) {
    while (true) {
      int i1 = inString.indexOf('\"');
      if (i1 == -1) {
        return inString;
      }
      int i2 = inString.indexOf('\"', i1 + 1);
      if (i2 == -1) {
        return inString;
      }
      inString = inString.substring(0, i1) + inString.substring(i1 + 1, i2).replace(' ', '\u1234') + inString.substring(i2 + 1);
    }
  }

  private static String[] expandBackslash(String inString) {
    StringBuilder out = new StringBuilder();
    char inCA[] = inString.toCharArray();
    for (int a = 0; a < inCA.length; a++) {
      if (inCA[a] == '\\') {
        if (inCA[a + 1] == '\\') {
          if (inCA[a + 2] == '\\') {
            if (inCA[a + 3] == '\\') {
              out.append('\\');
              out.append('\\');
              a += 3;
              continue;
            }
          } else if (inCA[a + 2] == ' ') {
            out.append('\u1234');
            a += 2;
            continue;
          } else {
            out.append('\\');
            a++;
            continue;
          }
        }
      }
      out.append(inCA[a]);
    }
    String cmd[] = out.toString().split(" ");
    for (int a = 0; a < cmd.length; a++) {
      if (cmd[a].indexOf('\u1234') != -1) {
        cmd[a] = cmd[a].replace('\u1234', ' ');
      }
    }
    return cmd;
  }

  /**
   * Currently only supports %f,%F,%u,%U
   */
  public static String[] expandDesktopExec(String exec, String file) {
    if (file == null) {
      file = "";
    }
    file = '\"' + file + '\"';
    exec = exec.replaceAll("%f", file);
    exec = exec.replaceAll("%F", file);
    exec = exec.replaceAll("%u", file);
    exec = exec.replaceAll("%U", file);
    exec = expandQuotes(exec);
    return expandBackslash(exec);
  }

  /**
   * Currently only supports %f,%F,%u,%U
   */
  public static String[] expandDesktopExec(String exec, String file[]) {
    String files = "";
    if (file != null) {
      for (int a = 0; a < file.length; a++) {
        if (a > 0) {
          files += " ";
        }
        files += '\"' + file[a] + '\"';
      }
    } else {
      file = new String[1];
      file[0] = "";
    }
    exec = exec.replaceAll("%f", '\"' + file[0] + '\"');
    exec = exec.replaceAll("%F", files);
    exec = exec.replaceAll("%u", '\"' + file[0] + '\"');
    exec = exec.replaceAll("%U", files);
    exec = expandQuotes(exec);
    return expandBackslash(exec);
  }

  public static int detectBits() {
    if (new File("/usr/lib64").exists()) {
      return 64;
    }
    return 32;
  }

  /**
   * Runs a bash script as root
   */
  public static boolean runScript(String lns[]) {
    try {
      File tmpFile = File.createTempFile("script", ".sh", new File("/tmp"));
      FileOutputStream fos = new FileOutputStream(tmpFile);
      fos.write("#!/bin/bash\n".getBytes());
      for (int a = 0; a < lns.length; a++) {
        fos.write((lns[a] + "\n").getBytes());
      }
      fos.close();
      tmpFile.setExecutable(true);
      ShellProcess sp = new ShellProcess();
      String output = sp.run(new String[]{"sudo", tmpFile.getAbsolutePath()}, true);
      tmpFile.delete();
      return sp.getErrorLevel() == 0;
    } catch (Exception e) {
      JFLog.log(e);
      return false;
    }
  }

  //use jna here to access native libraries

  //NOTE:in c++ the 'long' is 32 or 64bits (in Linux) so I use a Pointer which will be the same size
  private static interface X11 extends Library {
    Pointer XOpenDisplay(Pointer ptr);
    int DefaultScreen(Pointer ptr);
    NativeLong RootWindow(Pointer ptr, int screen);
    void XCloseDisplay(Pointer ptr);
    int XFetchName(Pointer ptr, NativeLong window, PointerByReference name);
    int XQueryTree(Pointer ptr, NativeLong window, PointerByReference dummy1, PointerByReference dummy2
      , PointerByReference children, IntByReference nchildren);
    void XFree(Pointer ptr);
    NativeLong XInternAtom(Pointer ptr, String str, int bool);
    int XChangeProperty(Pointer ptr, NativeLong xid, NativeLong state, NativeLong type, int size, int action
      , Pointer data, int cnt);
    int XKeysymToKeycode(Pointer ptr, char keysym);
    void XGetInputFocus(Pointer ptr, NativeLongByReference window, IntByReference revert);
    int XSendEvent(Pointer ptr, NativeLong window, int state, int type, Pointer event);
    NativeLong XDefaultRootWindow(Pointer ptr);
    NativeLong XCreateSimpleWindow(Pointer display, NativeLong parent, int x, int y, int width, int height, int border_width
      , NativeLong border, NativeLong background);
    void XSetSelectionOwner(Pointer display, NativeLong selection, NativeLong owner, NativeLong time);
    void XMapWindow(Pointer display, NativeLong window);
    void XUnmapWindow(Pointer display, NativeLong window);
    void XRaiseWindow(Pointer display, NativeLong window);
    void XNextEvent(Pointer display, XEvent ev);
    void XSelectInput(Pointer display, NativeLong window, NativeLong event_mask);
    void XMoveWindow(Pointer display, NativeLong window, int x, int y);
    void XMoveResizeWindow(Pointer display, NativeLong window, int x, int y, int width, int height);
    void XReparentWindow(Pointer display, NativeLong window, NativeLong parent, int x, int y);
    int XGetWindowProperty(Pointer display, NativeLong window, NativeLong prop, NativeLong offset, NativeLong length
      , int delete, NativeLong reqType, NativeLongByReference actType, IntByReference actFormat
      , NativeLongByReference nItems, NativeLongByReference bytesAfter, PointerByReference returnProp);
    int XGetClassHint(Pointer display, NativeLong w, XClassHint hint);
  }

  private static interface PAM extends Library {
    int pam_start(String ctx, String user, pam_conv conv, PointerByReference handle_ref);
    int pam_authenticate(Pointer handle, int flags);
    int pam_end(Pointer handle, int flags);
  }

  private static interface C extends Library {
    Pointer malloc(int size);
    Pointer calloc(int cnt, int size);
    void free(Pointer ptr);
    Pointer strdup(String str);
  }

  private static X11 x11;
  private static PAM pam;
  private static C c;

  public static class XEvent extends Structure {
    public int type;
    public NativeLong pad[] = new NativeLong[24];

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {
        "type", "pad"
      });
    }
  }

  public static class XKeyEvent extends Structure {
    public int type;
    public NativeLong serial;
    public int send_event;  //Bool
    public Pointer display;
    public NativeLong window, root, subwindow;
    public NativeLong time;  //???
    public int x,y,x_root,y_root;
    public int state;
    public int keycode;
    public int same_screen;  //Bool

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {
        "type", "serial" ,"send_event", "display", "window", "root", "subwindow"
        ,"time","x","y","x_root","y_root","state","keycode","same_screen"
      });
    }
  }
  private static final int KeyPress = 2;
  private static final int KeyRelease = 3;
  private static final int None = 0;
  private static final int CurrentTime = 0;
  private static final int ShiftMask = 1;
  private static final int True = 1;
  private static final int False = 0;
  private static final int KeyPressMask = 1;
  private static final int ClientMessage = 33;
  private static final int CreateNotify = 16;
  private static final int DestroyNotify = 17;
  private static final int UnmapNotify = 18;
  private static final int MapNotify = 19;
  private static final int ReparentNotify = 21;
  private static final int StructureNotifyMask = (1 << 17);
  private static final int SubstructureNotifyMask = (1 << 19);
  private static final int PropertyChangeMask = (1 << 22);

  public static class XClientMessageEvent extends Structure {
    public int type;
    public NativeLong serial;
    public int send_event;  //Bool
    public Pointer display;
    public NativeLong window;
    public NativeLong message_type;  //Atom
    public int format;
    //union {
    //public byte data_b[] = new byte[20];
    //public short data_s[] = new short[10];
    public NativeLong data_l[] = new NativeLong[5];
    //} data;

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {
        "type", "serial", "send_event", "display", "window", "message_type", "format", "data_l"
      });
    }
    public XClientMessageEvent(Pointer ptr) {
      super(ptr);
    }
  }

  public static class XDestroyWindowEvent extends Structure {
    public int type;
    public NativeLong serial;
    public int send_event;  //Bool
    public NativeLong display;
    public NativeLong event;
    public NativeLong window;

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {
        "type", "serial", "send_event", "display", "event", "window"
      });
    }
    public XDestroyWindowEvent(Pointer ptr) {
      super(ptr);
    }
  }

  public static class XCreateWindowEvent extends Structure {
    public int type;
    public NativeLong serial;
    public int send_event;  //Bool
    public NativeLong display;
    public NativeLong parent;
    public NativeLong window;
    public int x,y,width,height;
    public int border_width;
    public int override_redirect;  //Bool

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {
        "type", "serial", "send_event", "display", "parent", "window", "x", "y", "width", "height",
        "border_width", "override_redirect"
      });
    }
    public XCreateWindowEvent(Pointer ptr) {
      super(ptr);
    }
  }

  public static class XClassHint extends Structure {
    public Pointer res_name, res_class;

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {
        "res_name", "res_class"
      });
    }
  }

  public static boolean init() {
    //load x11 library
    if (x11 == null) {
      x11 = (X11)Native.loadLibrary("X11", X11.class);
    }
    //load PAM library
    if (pam == null) {
      pam = (PAM)Native.loadLibrary("pam", PAM.class);
    }
    //load C library
    if (c == null) {
      c = (C)Native.loadLibrary("c", C.class);
    }
    return x11 != null && pam != null && c != null;
  }

  public static Object x11_get_id(Window w) {
    return new NativeLong(Native.getWindowID(w));
  }

  private static final NativeLong AnyPropertyType = new NativeLong(0);
  private static final NativeLong XA_ATOM = new NativeLong(4);
  private static final NativeLong XA_CARDINAL = new NativeLong(6);
  private static final int PropModeReplace = 0;

  public static void x11_set_desktop(Object xid) {
    Pointer display = x11.XOpenDisplay(null);
    int ret;
    Pointer states = c.malloc(NativeLong.SIZE * 4);
    Pointer types = c.malloc(NativeLong.SIZE * 1);
    for(int a=0;a<2;a++) {
      NativeLong state = x11.XInternAtom(display, "_NET_WM_STATE", 0);
      states.setNativeLong(NativeLong.SIZE * 0, x11.XInternAtom(display, "_NET_WM_STATE_BELOW", 0));
      states.setNativeLong(NativeLong.SIZE * 1, x11.XInternAtom(display, "_NET_WM_STATE_SKIP_PAGER", 0));
      states.setNativeLong(NativeLong.SIZE * 2, x11.XInternAtom(display, "_NET_WM_STATE_SKIP_TASKBAR", 0));
      states.setNativeLong(NativeLong.SIZE * 3, x11.XInternAtom(display, "_NET_WM_STATE_STICKY", 0));
      ret = x11.XChangeProperty(display, (NativeLong)xid, state, XA_ATOM, 32, PropModeReplace, states, 4);
      NativeLong type = x11.XInternAtom(display, "_NET_WM_WINDOW_TYPE", 0);
      types.setNativeLong(NativeLong.SIZE * 0, x11.XInternAtom(display, "_NET_WM_WINDOW_TYPE_DESKTOP", 0));
      ret = x11.XChangeProperty(display, (NativeLong)xid, type, XA_ATOM, 32, PropModeReplace, types, 1);
    }
    c.free(states);
    c.free(types);
    x11.XCloseDisplay(display);
  }

  public static void x11_set_dock(Object xid) {
    Pointer display = x11.XOpenDisplay(null);
    int ret;
    Pointer states = c.malloc(Pointer.SIZE * 4);
    Pointer types = c.malloc(Pointer.SIZE * 1);
    for(int a=0;a<2;a++) {
      NativeLong state = x11.XInternAtom(display, "_NET_WM_STATE", 0);
      states.setNativeLong(NativeLong.SIZE * 0, x11.XInternAtom(display, "_NET_WM_STATE_ABOVE", 0));
      states.setNativeLong(NativeLong.SIZE * 1, x11.XInternAtom(display, "_NET_WM_STATE_SKIP_PAGER", 0));
      states.setNativeLong(NativeLong.SIZE * 2, x11.XInternAtom(display, "_NET_WM_STATE_SKIP_TASKBAR", 0));
      states.setNativeLong(NativeLong.SIZE * 3, x11.XInternAtom(display, "_NET_WM_STATE_STICKY", 0));
      ret = x11.XChangeProperty(display, (NativeLong)xid, state, XA_ATOM, 32, PropModeReplace, states, 4);
      NativeLong type = x11.XInternAtom(display, "_NET_WM_WINDOW_TYPE", 0);
      types.setNativeLong(NativeLong.SIZE * 0, x11.XInternAtom(display, "_NET_WM_WINDOW_TYPE_DOCK", 0));
      ret = x11.XChangeProperty(display, (NativeLong)xid, type, XA_ATOM, 32, PropModeReplace, types, 1);
    }
    c.free(states);
    c.free(types);
    x11.XCloseDisplay(display);
  }

  public static void x11_set_strut(Object xid, int panelHeight, int x, int y, int width, int height) {
    Pointer display = x11.XOpenDisplay(null);
    NativeLong strut = x11.XInternAtom(display, "_NET_WM_STRUT_PARTIAL", 0);
    Pointer values = c.malloc(NativeLong.SIZE * 12);
    values.setNativeLong(NativeLong.SIZE * 0, new NativeLong(0));  //left
    values.setNativeLong(NativeLong.SIZE * 1, new NativeLong(0));  //right
    values.setNativeLong(NativeLong.SIZE * 2, new NativeLong(0));  //top
    values.setNativeLong(NativeLong.SIZE * 3, new NativeLong(panelHeight));   //bottom
    values.setNativeLong(NativeLong.SIZE * 4, new NativeLong(0));  //left_start_y
    values.setNativeLong(NativeLong.SIZE * 5, new NativeLong(height-1));  //left_end_y
    values.setNativeLong(NativeLong.SIZE * 6, new NativeLong(0));  //right_start_y
    values.setNativeLong(NativeLong.SIZE * 7, new NativeLong(height-1));  //right_end_y
    values.setNativeLong(NativeLong.SIZE * 8, new NativeLong(0));  //top_start_x
    values.setNativeLong(NativeLong.SIZE * 9, new NativeLong(width-1));  //top_end_x
    values.setNativeLong(NativeLong.SIZE * 10, new NativeLong(0));  //bottom_start_x
    values.setNativeLong(NativeLong.SIZE * 11, new NativeLong(width-1));  //bottom_end_x
    int ret = x11.XChangeProperty(display, (NativeLong)xid, strut, XA_CARDINAL, 32, PropModeReplace, values, 12);
    c.free(values);

    x11.XCloseDisplay(display);
  }

//tray functions

  private static final int MAX_TRAY_ICONS = 64;
  private static NativeLong tray_icons[] = new NativeLong[MAX_TRAY_ICONS];
  private static int screen_width;
  private static Pointer tray_display;
  private static NativeLong tray_opcode;//, tray_data;
  private static NativeLong tray_window;
  private static boolean tray_active;
  private static int tray_count = 0;
  private static X11Listener x11_listener;

  private static void tray_move_icons() {
    int a, x = 2, y = 2;
    for(a=0;a<MAX_TRAY_ICONS;a++) {
      if (tray_icons[a] == null) continue;
      x11.XMoveWindow(tray_display, tray_icons[a], x, y);
      if (y == 2) {
        y += 23 + 2;
      } else {
        y = 2;
        x += 23 + 2;
      }
    }
    //reposition/resize tray window
    int cols = (tray_count + 1) / 2;
    if (cols == 0) cols = 1;
    x11.XMoveResizeWindow(tray_display, tray_window, screen_width - cols * (23+2) - 2 - 5 , 5
      , cols * (23+2) + 2, 52);
  }

  private static void tray_add_icon(NativeLong w) {
    if (tray_count == MAX_TRAY_ICONS) return;  //ohoh
    tray_count++;
    int a;
    for(a=0;a<MAX_TRAY_ICONS;a++) {
      if (tray_icons[a] == null) {
        tray_icons[a] = w;
        break;
      }
    }
    x11.XReparentWindow(tray_display, w, tray_window, 0, 0);
    tray_move_icons();
    x11.XMapWindow(tray_display, w);
    x11_listener.trayIconAdded(tray_count);
  }

  /* Tray opcode messages from System Tray Protocol Specification
   * http://freedesktop.org/Standards/systemtray-spec/systemtray-spec-0.2.html */
  private static final int SYSTEM_TRAY_REQUEST_DOCK   = 0;
  private static final int SYSTEM_TRAY_BEGIN_MESSAGE  = 1;
  private static final int SYSTEM_TRAY_CANCEL_MESSAGE = 2;

  private static void tray_client_message(XClientMessageEvent ev) {
    if (ev.message_type.equals(tray_opcode)) {
      switch (ev.data_l[1].intValue()) {
        case SYSTEM_TRAY_REQUEST_DOCK:
          tray_add_icon(ev.data_l[2]);
          break;
        case SYSTEM_TRAY_BEGIN_MESSAGE:
          break;
        case SYSTEM_TRAY_CANCEL_MESSAGE:
          break;
      }
    }
  }

  private static void tray_remove_icon(XDestroyWindowEvent ev) {
    int a;
    for(a=0;a<MAX_TRAY_ICONS;a++) {
      if (tray_icons[a] == null) continue;
      if (tray_icons[a].equals(ev.window)) {
        tray_icons[a] = null;
        tray_count--;
        tray_move_icons();
        x11_listener.trayIconRemoved(tray_count);
        break;
      }
    }
  }

  public static void x11_set_listener(X11Listener cb) {
    x11_listener = cb;
  }

  /** Polls and dispatches tray events.  Does not return. */
  public static void x11_tray_main(Object pid, int width) {
    XEvent ev = new XEvent();

    screen_width = width;
    tray_display = x11.XOpenDisplay(null);
    NativeLong tray_atom = x11.XInternAtom(tray_display, "_NET_SYSTEM_TRAY_S0", False);
    tray_opcode = x11.XInternAtom(tray_display, "_NET_SYSTEM_TRAY_OPCODE", False);
//    tray_data = x11.XInternAtom(tray_display, "_NET_SYSTEM_TRAY_MESSAGE_DATA", False);

    tray_window = x11.XCreateSimpleWindow(
      tray_display,
      (NativeLong)pid,  //parent id
      width - 23 - 4 - 5, 5,  //pos
      23 + 4, 52,  //size
      1,  //border_width
      new NativeLong(0xcccccc),  //border clr
      new NativeLong(0xdddddd));  //backgnd clr

    x11.XSetSelectionOwner(tray_display, tray_atom, tray_window, new NativeLong(CurrentTime));

    //get DestroyNotify events
    x11.XSelectInput(tray_display, tray_window, new NativeLong(SubstructureNotifyMask));

    x11.XMapWindow(tray_display, tray_window);

    tray_active = true;
    while (tray_active) {
      x11.XNextEvent(tray_display, ev);
      switch (ev.type) {
        case ClientMessage:
          XClientMessageEvent xclient = new XClientMessageEvent(ev.getPointer());
          xclient.read();
          tray_client_message(xclient);
          break;
        case DestroyNotify:
          XDestroyWindowEvent xdestroywindow = new XDestroyWindowEvent(ev.getPointer());
          xdestroywindow.read();
          tray_remove_icon(xdestroywindow);
          break;
      }
    }

    x11.XCloseDisplay(tray_display);
  }

  public static void x11_tray_stop() {
    tray_active = false;
    //TODO : send a message to tray_window to cause main() loop to abort
  }

//top-level x11 windows monitor

  private static boolean window_list_active = false;

  /** Polls and dispatches top-level windows events.  Does not return. */
  public static void x11_window_list_main() {
    XEvent ev = new XEvent();

    Pointer display = x11.XOpenDisplay(null);
//    NativeLong client_list = x11.XInternAtom(display, "_NET_CLIENT_LIST", False);
    NativeLong root_window = x11.XDefaultRootWindow(display);

    //get DestroyNotify events
    x11.XSelectInput(display, root_window, new NativeLong(SubstructureNotifyMask));

    window_list_active = true;
    while (window_list_active) {
      x11.XNextEvent(display, ev);
      switch (ev.type) {
        case DestroyNotify:
          XDestroyWindowEvent xdestroywindow = new XDestroyWindowEvent(ev.getPointer());
          xdestroywindow.read();
          x11_listener.windowRemoved();
          break;
        case CreateNotify:
          XCreateWindowEvent xcreatewindow = new XCreateWindowEvent(ev.getPointer());
          xcreatewindow.read();
          x11_listener.windowRemoved();
          break;
      }
    }

    x11.XCloseDisplay(display);
  }

  public static void x11_window_list_stop() {
    window_list_active = false;
    //TODO : send a message to ??? to cause main() loop to abort
  }

  public static class TopLevelWindow {
    public Object xid;
    public int pid;
    public String title;  //_NET_WM_NAME
    public String name;  //XFetchName
    public String res_name, res_class;
    public TopLevelWindow(Object xid, int pid, String title, String name, XClassHint hint) {
      this.xid = xid;
      this.pid = pid;
      this.title = title;
      this.name = name;
      if (hint.res_name != null)
        res_name = hint.res_name.getString(0);
      else
        res_name = "";
      if (hint.res_class != null)
        res_class = hint.res_class.getString(0);
      else
        res_class = "";
    }
  }

  /** Returns details of all top-level windows */
  public static java.util.List<TopLevelWindow> x11_get_window_list() {
    ArrayList<TopLevelWindow> topLevelWindows = new ArrayList<TopLevelWindow>();

    Pointer display = x11.XOpenDisplay(null);
    NativeLong root_window = x11.XDefaultRootWindow(display);

    NativeLong net_name = x11.XInternAtom(display, "_NET_WM_NAME", False);
    NativeLong net_pid = x11.XInternAtom(display, "_NET_WM_PID", False);
    NativeLong net_skip_taskbar = x11.XInternAtom(display, "_NET_WM_STATE_SKIP_TASKBAR", False);

    PointerByReference childrenRef = new PointerByReference();
    IntByReference nchildren = new IntByReference();
    int res = x11.XQueryTree(display, root_window, new PointerByReference(), new PointerByReference(), childrenRef
      , nchildren);

    if (res != 0) {
      int cnt = nchildren.getValue();
      Pointer list = childrenRef.getValue();
      for(int a=0;a<cnt;a++) {
        NativeLong xid = list.getNativeLong(NativeLong.SIZE * a);

        PointerByReference propRef = new PointerByReference();
        NativeLongByReference nItemsRef = new NativeLongByReference();
        int nItems;

        //has skip taskbar???
        x11.XGetWindowProperty(display, xid, net_skip_taskbar, new NativeLong(0), new NativeLong(1024), False, AnyPropertyType
          , new NativeLongByReference(), new IntByReference(), nItemsRef, new NativeLongByReference(), propRef);
        nItems = nItemsRef.getValue().intValue();
        if (nItems > 0) {
          x11.XFree(propRef.getValue());
          continue;
        }

        //get window pid
        propRef = new PointerByReference();
        nItemsRef = new NativeLongByReference();
        x11.XGetWindowProperty(display, xid, net_pid, new NativeLong(0), new NativeLong(1024), False, AnyPropertyType
          , new NativeLongByReference(), new IntByReference(), nItemsRef, new NativeLongByReference(), propRef);
        nItems = nItemsRef.getValue().intValue();
        int pid = -1;
        if (nItems > 0) {
          Pointer prop = propRef.getValue();
          pid = (int)prop.getInt(0);
          x11.XFree(prop);
        }

        //get title
        propRef = new PointerByReference();
        nItemsRef = new NativeLongByReference();
        x11.XGetWindowProperty(display, xid, net_name, new NativeLong(0), new NativeLong(1024), False, AnyPropertyType
          , new NativeLongByReference(), new IntByReference(), nItemsRef, new NativeLongByReference(), propRef);
        nItems = nItemsRef.getValue().intValue();
        String title = "";
        if (nItems > 0) {
          Pointer prop = propRef.getValue();
          title = prop.getString(0);
          x11.XFree(prop);
        }

        String name = "";
        PointerByReference nameRef = new PointerByReference();
        x11.XFetchName(display, xid, nameRef);
        Pointer namePtr = nameRef.getValue();
        if (namePtr != null) {
          name = namePtr.getString(0);
          x11.XFree(namePtr);
        }

        XClassHint hint = new XClassHint();
        x11.XGetClassHint(display, xid, hint);
        topLevelWindows.add(new TopLevelWindow(xid, pid, title, name, hint));
        if (hint.res_name != null) {
          x11.XFree(hint.res_name);
          hint.res_name = null;
        }
        if (hint.res_class != null) {
          x11.XFree(hint.res_class);
          hint.res_class = null;
        }
      }
      x11.XFree(list);
    }

    x11.XCloseDisplay(display);

    return topLevelWindows;
  }

  public static void x11_raise_window(Object xid) {
    Pointer display = x11.XOpenDisplay(null);
    x11.XRaiseWindow(display, (NativeLong)xid);
    x11.XCloseDisplay(display);
  }

  public static void x11_map_window(Object xid) {
    Pointer display = x11.XOpenDisplay(null);
    x11.XMapWindow(display, (NativeLong)xid);
    x11.XCloseDisplay(display);
  }

  public static void x11_unmap_window(Object xid) {
    Pointer display = x11.XOpenDisplay(null);
    x11.XUnmapWindow(display, (NativeLong)xid);
    x11.XCloseDisplay(display);
  }

//x11 send event functions

  public static int x11_keysym_to_keycode(char keysym) {
    Pointer display = x11.XOpenDisplay(null);
    int keycode = x11.XKeysymToKeycode(display, keysym);
    x11.XCloseDisplay(display);
    switch (keysym) {
      case '!':
      case '@':
      case '#':
      case '$':
      case '%':
      case '^':
      case '&':
      case '*':
      case '"':
      case ':':
        keycode |= 0x100;
    }
    return keycode;
  }

  public static boolean x11_send_event(int keycode, boolean down) {
    Pointer display = x11.XOpenDisplay(null);

    NativeLongByReference x11id = new NativeLongByReference();
    IntByReference revert = new IntByReference();
    x11.XGetInputFocus(display, x11id, revert);

    XKeyEvent event = new XKeyEvent();

    event.type = (down ? KeyPress : KeyRelease);
    event.keycode = keycode & 0xff;
    event.display = display;
    event.window = x11id.getValue();
    event.root = x11.XDefaultRootWindow(display);
    event.subwindow = new NativeLong(None);
    event.time = new NativeLong(CurrentTime);
    event.x = 1;
    event.y = 1;
    event.x_root = 1;
    event.y_root = 1;
    event.same_screen = True;
    if ((keycode & 0x100) == 0x100) event.state = ShiftMask;
    event.write();

    int status = x11.XSendEvent(display, event.window, True, KeyPressMask, event.getPointer());

    x11.XCloseDisplay(display);

    return status != 0;
  }

  //PAM functions

  public static class pam_message extends Structure {
    public int msg_style;
    public String msg;

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {"msg_style", "msg"});
    }
    public pam_message(Pointer ptr) {
      super(ptr);
    }
  }

  public static class pam_response extends Structure {
    public Pointer resp;
    public int resp_retcode;

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {"resp", "resp_retcode"});
    }
    public pam_response(Pointer ptr) {
      super(ptr);
    }
  }

  public static class pam_conv extends Structure {
    public Callback func;
    public Pointer appdata_ptr;

    @Override
    protected java.util.List getFieldOrder() {
      return Arrays.asList(new String[] {"func", "appdata_ptr"});
    }
  }

  public static interface do_conv extends Callback {
    public int callback(int num_msg, Pointer _pam_messages, Pointer _pam_responses, Pointer _appdata_ptr);
  }

  public static do_conv pam_callback = new do_conv() {
    public int callback(int num_msg, Pointer _pam_messages, Pointer _pam_responses, Pointer _appdata_ptr) {
      int resp_size = Pointer.SIZE + 4;  //sizeof(pam_response)
      pam_responses = c.calloc(num_msg, resp_size);  //array of pam_response
      Pointer tmp;
      for(int a=0;a<num_msg;a++) {
        pam_message msg = new pam_message(_pam_messages.getPointer(a * Pointer.SIZE));
        msg.read();
        tmp = null;
        switch (msg.msg_style) {
          case PAM_PROMPT_ECHO_ON:
            tmp = c.strdup(pam_user);
            break;
          case PAM_PROMPT_ECHO_OFF:
            tmp = c.strdup(pam_pass);
            break;
        }
        pam_responses.setPointer(a * resp_size, tmp);  //pam_response.resp
        pam_responses.setInt(a * resp_size + Pointer.SIZE, 0);  //pam_response.resp_retcode
      }
      _pam_responses.setPointer(0, pam_responses);
      return 0;
    }
  };

  private static final int PAM_SILENT = 0x8000;
  private static final int PAM_PROMPT_ECHO_ON = 2;
  private static final int PAM_PROMPT_ECHO_OFF = 1;

  private static String pam_user, pam_pass;
  private static Pointer pam_responses;

  public static synchronized boolean authUser(String user, String pass) {
    pam_conv conv = new pam_conv();
    pam_user = user;
    pam_pass = pass;
    conv.func = pam_callback;
    PointerByReference ref = new PointerByReference();
    Pointer handle;

    int res = pam.pam_start("passwd", user, conv, ref);
    if (res != 0) return false;
    handle = ref.getValue();
    res = pam.pam_authenticate(handle, PAM_SILENT);
    pam.pam_end(handle, 0);
    if (pam_responses != null) {
//      c.free(pam_responses);  //crashes if password was wrong - memory leak for now???
      pam_responses = null;
    }

    pam_user = null;
    pam_pass = null;

    return res == 0;
  }

  //test some x11 stuff
  public static void main(String args[]) {
    if (!init()) {
      System.out.println("init() failed");
      return;
    }
    java.util.List<TopLevelWindow> list = x11_get_window_list();
    for(int a=0;a<list.size();a++) {
      TopLevelWindow tlw = list.get(a);
      System.out.println("xid=" + tlw.xid + ",pid=" + tlw.pid + ",name=" + tlw.name + ",res_name="
        + tlw.res_name + ",res_class=" + tlw.res_class);
    }
  }
}
