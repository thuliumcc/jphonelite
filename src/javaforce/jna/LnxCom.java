package javaforce.jna;

/**
 * Linux Com Port I/O (RS-232)
 *
 * @author pquiring
 *
 * Created : Jan 17, 2014
 */

import java.lang.reflect.*;
import java.util.*;

import com.sun.jna.*;

import javaforce.*;

public class LnxCom {
  private LnxCom() {}
  private static Pointer malloc(int size) {
    return new Pointer(Native.malloc(size));
  }

  private static void free(Pointer ptr) {
    Native.free(Pointer.nativeValue(ptr));
  }

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

  public static interface C extends Library {
    public int open(String name, int access);
    public void close(int fd);
    public int read(int fd, Pointer buffer, int size);
    public int write(int fd, Pointer buffer, int size);
    public int tcgetattr(int fd, termios attrs);
    public int tcsetattr(int fd, int optacts, termios attrs);
    public void cfsetispeed(termios attrs, int code);
    public void cfsetospeed(termios attrs, int code);
    public void tcflush(int fd, int code);
  }

  public static C c;

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

  private static final int O_RDWR = 02;
  private static final int O_NONBLOCK = 04000;
  private static final int O_NOCTTY = 0400;

  private static final int PARENB = 0400;
  private static final int CSTOPB = 0100;
  private static final int CSIZE = 060;
  private static final int CS8 = 060;
  private static final int CRTSCTS = 020000000000;
  private static final int CREAD = 0200;
  private static final int CLOCAL = 04000;
  private static final int IXON = 02000;
  private static final int IXANY = 04000;
  private static final int IXOFF = 010000;
  private static final int ICANON = 02;
  private static final int ECHO = 010;
  private static final int ECHOE = 020;
  private static final int ISIG = 01;
  private static final int OPOST = 01;
  private static final int VMIN = 6;
  private static final int VTIME = 5;
  private static final int TCIFLUSH = 0;
  private static final int TCSANOW = 0;

  private int fd = -1;
  private termios orgattrs = new termios();

  //assumes 8 data bits, 1 stop bit, no parity, etc.
  public static LnxCom open(String name, int baud) {
    int baudcode = -1;
    switch (baud) {
      case 9600: baudcode = 015; break;
      case 19200: baudcode = 016; break;
      case 38400: baudcode = 017; break;
      case 57600: baudcode = 010001; break;
      case 115200: baudcode = 010002; break;
    }
    if (baudcode == -1) {
      JFLog.log("LnxCom:Unknown baud rate");
      return null;
    }
    LnxCom wc = new LnxCom();
    wc.fd = c.open(name, O_RDWR | O_NOCTTY);
    if (wc.fd == -1) {
      JFLog.log("LnxCom:invalid handle");
      return null;
    }
    c.tcgetattr(wc.fd, wc.orgattrs);
    termios attrs = new termios();
    attrs.c_cflag = baudcode | CS8 | CLOCAL | CREAD;

    attrs.c_cc[VMIN]  =  1;          // block until at least 1 char
    attrs.c_cc[VTIME] =  5;          // 0.5 seconds read timeout

    c.tcflush(wc.fd, TCIFLUSH);
    c.tcsetattr(wc.fd, TCSANOW, attrs);
    return wc;
  }

  public byte[] read(int bufsize) {
    Pointer buf = malloc(bufsize);
    int read = c.read(fd, buf, bufsize);
    if (read <= 0) {
      free(buf);
      return null;
    }
    byte data[] = buf.getByteArray(0, read);
    free(buf);
    return data;
  }

  public int write(byte data[]) {
    Pointer buf = malloc(data.length);
    buf.write(0, data, 0, data.length);
    int write = c.write(fd, buf, data.length);
    free(buf);
    return write;
  }

  public void close() {
    if (fd != -1) {
      c.tcsetattr(fd, TCSANOW, orgattrs);
      c.close(fd);
      fd = -1;
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
}
