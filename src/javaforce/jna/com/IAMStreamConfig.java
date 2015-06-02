package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

public class IAMStreamConfig extends IUnknown {
  public static class ByReference extends IAMStreamConfig implements Structure.ByReference {}
  public IAMStreamConfig() {}
  public IAMStreamConfig(Pointer pvInstance) {
    super(pvInstance);
  }
  //3 = SetFormat
  //4 = GetFormat
  public int GetFormat(PointerByReference mediaType) {
    return invokeInt(4, new Object[] { getPointer(), mediaType });
  }
  //5 = GetNumberOfCapabilities
  //6 = GetStreamCaps
}
