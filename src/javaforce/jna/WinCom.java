package javaforce.jna;

/**
 * Windows Com Port I/O (RS-232)
 *
 * @author pquiring
 *
 * Created : Jan 16, 2014
 */

import java.lang.reflect.*;
import java.util.*;

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.*;

import javaforce.*;

public class WinCom {
  private WinCom() {}
  private static Pointer malloc(int size) {
    return new Pointer(Native.malloc(size));
  }

  private static void free(Pointer ptr) {
    Native.free(Pointer.nativeValue(ptr));
  }

  public static class DCB extends Structure {
    public int DCBlength, baud, flags;
    public short resv, XonLim, XoffLim;
    public byte ByteSize, Parity, StopBits;
    public byte XonChar, XoffChar, ErrorChar, EofChar, EvtChar;  //char
    public short resv2;
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
  }
  public static class COMMTIMEOUTS extends Structure {
    public int ReadIntervalTimeout, ReadTotalTimeoutMultiplier, ReadTotalTimeoutConstant;
    public int WriteTotalTimeoutMultiplier, WriteTotalTimeoutConstant;
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
  }
  public static interface Kernel extends StdCallLibrary {
    public Pointer CreateFileA(String name, int desiredAccess, int shareMode, Pointer sec, int creationDisp, int flags, Pointer template);
    public void CloseHandle(Pointer handle);
    public void ReadFile(Pointer handle, Pointer buffer, int size, IntByReference sizeRead, Pointer overLapped);
    public void WriteFile(Pointer handle, Pointer buffer, int size, IntByReference sizeRead, Pointer overLapped);
    public boolean GetCommState(Pointer handle, DCB dcb);
    public boolean SetCommState(Pointer handle, DCB dcb);
    public boolean WaitCommEvent(Pointer handle, IntByReference events, Pointer overLapped);
    public boolean GetCommTimeouts(Pointer handle, COMMTIMEOUTS cto);
    public boolean SetCommTimeouts(Pointer handle, COMMTIMEOUTS cto);
  }

  public static final int GENERIC_READ = 0x80000000;
  public static final int GENERIC_WRITE = 0x40000000;
  public static final int OPEN_EXISTING = 3;
  public static final int ONESTOPBIT = 0;

  public static Kernel kernel;
  public static Pointer InvalidHandle = new Pointer(-1);

  public static boolean init() {
    if (kernel != null) return true;
    try {
      kernel = (Kernel)Native.loadLibrary("kernel32", Kernel.class);
      return true;
    } catch (Throwable t) {
      JFLog.log(t);
      return false;
    }
  }
  //assumes 8 data bits, 1 stop bit, no parity, etc.
  public static WinCom open(String name, int baud) {
    WinCom wc = new WinCom();
    wc.handle = kernel.CreateFileA(name, GENERIC_READ | GENERIC_WRITE, 0, null, OPEN_EXISTING, 0, null);
    if (wc.handle.equals(InvalidHandle)) return null;
    DCB dcb = new DCB();
    dcb.DCBlength = dcb.size();
    kernel.GetCommState(wc.handle, dcb);
    dcb.baud = baud;
    dcb.flags = 1;  //fBinary is set, everything else is cleared
    dcb.ByteSize = 8;  //8 data bits
    dcb.StopBits = ONESTOPBIT;  //1 stop bit
    kernel.SetCommState(wc.handle, dcb);
    COMMTIMEOUTS cto = new COMMTIMEOUTS();
    kernel.GetCommTimeouts(wc.handle, cto);
    cto.ReadIntervalTimeout = 10;
    cto.ReadTotalTimeoutMultiplier = 0;
    cto.ReadTotalTimeoutConstant = 100;
    cto.WriteTotalTimeoutMultiplier = 0;
    cto.WriteTotalTimeoutConstant = 0;
    kernel.SetCommTimeouts(wc.handle, cto);
    return wc;
  }
  private Pointer handle;
  public byte[] read(int bufsize) {
    Pointer buf = malloc(bufsize);
    IntByReference intRef = new IntByReference();
    kernel.ReadFile(handle, buf, bufsize, intRef, null);
    int read = intRef.getValue();
    if (read <= 0) return null;
    byte data[] = buf.getByteArray(0, read);
    free(buf);
    return data;
  }
  public int write(byte data[]) {
    Pointer buf = malloc(data.length);
    buf.write(0, data, 0, data.length);
    IntByReference intRef = new IntByReference();
    kernel.WriteFile(handle, buf, data.length, intRef, null);
    free(buf);
    return intRef.getValue();
  }
  public void close() {
    if (handle != null) {
      kernel.CloseHandle(handle);
      handle = null;
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
