package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

public class IEnumMediaTypes extends IUnknown {
  public static class ByReference extends IEnumMediaTypes implements Structure.ByReference {}
  public IEnumMediaTypes() {}
  public IEnumMediaTypes(Pointer pvInstance) {
    super(pvInstance);
  }
  //3 = Next
  public int Next(int cnt, PointerByReference ref, IntByReference fetched) {
    return invokeInt(3, new Object[] { getPointer(), cnt, ref, fetched});
  }
  //4 = Skip
  //5 = Reset
  //6 = Clone
}
