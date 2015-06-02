package javaforce.jna;

/** Linux PTY support.
 *
 * @author pquiring
 *
 * Created : Jan 17, 2014
 */

import java.util.*;
import java.lang.reflect.*;

import com.sun.jna.*;

import javaforce.*;

public class LnxPty {

  public static class termios extends Structure {
    public int c_iflag;
    public int c_oflag;
    public int c_cflag;
    public int c_lflag;
    public byte c_line;
    public byte c_cc[] = new byte[32];
    public int c_ispeed;
    public int c_ospeed;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public termios() {}
    public termios(Pointer ptr) {
      super(ptr);
    }
  }

  public static class winsize extends Structure {
    public short ws_row;
    public short ws_col;
    public short ws_xpixel;
    public short ws_ypixel;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public winsize() {}
    public winsize(Pointer ptr) {
      super(ptr);
    }
  }

  public interface C extends Library {
    public int fork();
    public int posix_openpt(int o_flgs);
    public String ptsname(int fd);
    public int grantpt(int fd);
    public int unlockpt(int fd);
    public int ioctl(int fd, int cmd, winsize size);
    public int execvp(String cmd, String argv[]);
    public int execve(String cmd, String argv[], String env[]);
    public int open(String name, int flgs);
    public int close(int fd);
    public int tcgetattr(int fd, termios attrs);
    public int tcsetattr(int fd, int optacts, termios attrs);
    public int dup2(int fd, int fd2);
    public int signal(int sig, Pointer handler);
    public int read(int fd, Pointer buf, int bufsiz);
    public int write(int fd, Pointer buf, int bufsiz);
    public int setsid();
  }

  private static C c;
  private static final int O_RDWR = 02;
  private static final int O_NOCTTY = 0400;
  private static final int SIGINT = 2;
  private static final int SIGQUIT = 3;
  private static final int SIGCHLD = 17;
  private static final Pointer SIG_DFL = null;
  private static final int STDIN_FILENO = 0;
  private static final int STDOUT_FILENO = 1;
  private static final int STDERR_FILENO = 2;
  private static final int VERASE = 2;
  private static final int IUTF8 = 040000;
  private static final int IXON = 02000;
  private static final int TIOCSWINSZ = 0x5414;
  private static final int TCSANOW = 0;

  public static boolean init() {
    if (c != null) return true;
    try {
      c = (C)Native.loadLibrary("c", C.class);
      return true;
    } catch (Throwable t) {
      JFLog.log(t);
      return false;
    }
  }

  private static List makeFieldList(Class cls) {
    //This "assumes" compiler places fields in order as defined (some don't)
    ArrayList<String> list = new ArrayList<String>();
    Field fields[] = cls.getFields();
    for(int a=0;a<fields.length;a++) {
      String name = fields[a].getName();
      if (name.startsWith("ALIGN_")) continue;  //field of Structure
      list.add(name);
    }
    return list;
  }

  public static LnxPty exec(String cmd, String args[], String env[]) {
    LnxPty pty = new LnxPty();
    if (!pty.fork(cmd, args, env)) return null;
    return pty;
  }

  private int master = -1;
  private long writeBuf, readBuf;

  /** Spawns a new process in a new PTY.  Note:args, env MUST be null terminated. */
  private boolean fork(String cmd, String args[], String env[]) {
    String slaveName;
    master = c.posix_openpt(O_RDWR | O_NOCTTY);
    if (master == -1) return false;
//    JFLog.log("LnxPty:master=" + master);
    slaveName = c.ptsname(master);
//    JFLog.log("LnxPty:slave=" + slaveName);
    if (slaveName == null) return false;
    if (c.grantpt(master) != 0) return false;
    if (c.unlockpt(master) != 0) return false;
    String fullcmd = "/usr/bin/" + cmd;
    termios attrs = new termios();
    int slave = c.open(slaveName, O_RDWR);  //should open this in child process
    if (slave == -1) return false;
    int pid = c.fork();
    if (pid < 0) return false;
    if (pid == 0) {
      //run child (try and do as little here)
      if (c.setsid() == -1) System.exit(0);
      c.close(master);  //close master fd in child process
      c.tcgetattr(slave, attrs);
      // Assume input is UTF-8; this allows character-erase to be correctly performed in cooked mode.
      attrs.c_iflag |= IUTF8;
      // Humans don't need XON/XOFF flow control of output, and it only serves to confuse those who accidentally hit ^S or ^Q, so turn it off.
      attrs.c_iflag &= ~IXON;
      //???
      attrs.c_cc[VERASE] = 127;
      c.tcsetattr(slave, TCSANOW, attrs);
      c.dup2(slave, STDIN_FILENO);
      c.dup2(slave, STDOUT_FILENO);
      c.dup2(slave, STDERR_FILENO);
      c.signal(SIGINT, SIG_DFL);
      c.signal(SIGQUIT, SIG_DFL);
      c.signal(SIGCHLD, SIG_DFL);
      c.execve(fullcmd, args, env);  //does NOT search path
      System.exit(0);
      return false;  //should never happen
    } else {
      c.close(slave);  //should open in child process
      writeBuf = Native.malloc(1024);
      readBuf = Native.malloc(1024);
      return true;
    }
  }

  /** Frees resources */
  public void close() {
    if (master != -1) {
      c.close(master);
      master = -1;
    }
    if (writeBuf != 0) {
      Native.free(writeBuf);
      writeBuf = 0;
    }
    if (readBuf != 0) {
      Native.free(readBuf);
      readBuf = 0;
    }
  }

  /** Writes to child process (max 1024) */
  public void write(byte buf[]) {
    Pointer p = new Pointer(writeBuf);
    p.write(0, buf, 0, buf.length);
    c.write(master, p, buf.length);
  }

  /** Reads from child process (max 1024) */
  public int read(byte buf[]) {
    Pointer p = new Pointer(readBuf);
    int read = c.read(master, p, buf.length);
    if (read > 0) {
      p.read(0, buf, 0, read);
    }
    return read;
  }

  /** Sets the size of the pty */
  public void setSize(int x, int y) {
    winsize size = new winsize();
    size.ws_row = (short)y;
    size.ws_col = (short)x;
    size.ws_xpixel = (short)(x*8);
    size.ws_ypixel = (short)(y*8);
    c.ioctl(master, TIOCSWINSZ, size);
  }
}
