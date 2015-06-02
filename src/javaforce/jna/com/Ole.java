package javaforce.jna.com;

/**
 * Ole Library
 *
 * @author pquiring
 *
 * Created : Aug 17, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.*;

import javaforce.jna.com.Guid.*;

public interface Ole extends StdCallLibrary {
  public int CoInitialize(Pointer reserved);
  public int CoInitializeEx(Pointer reserved, int dwCoInit);
  int COINIT_APARTMENTTHREADED  = 0x2;
  int COINIT_MULTITHREADED      = 0x0;
  int COINIT_DISABLE_OLE1DDE    = 0x4;
  int COINIT_SPEED_OVER_MEMORY  = 0x8;
  public int CoCreateInstance(CLSID clsid, Pointer pUnkOuter, int dwClsContext, IID riid, PointerByReference ppv);
  int CLSCTX_INPROC_SERVER = 0x1;
  public int CoUninitialize();
  public int CLSIDFromProgID(String progId, CLSID clsid);
  public int CoTaskMemFree(Pointer pv);
}
