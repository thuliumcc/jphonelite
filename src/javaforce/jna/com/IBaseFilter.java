package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

public class IBaseFilter extends IMediaFilter {
  public static class ByReference extends IBaseFilter implements Structure.ByReference {}
  public IBaseFilter() {}
  public IBaseFilter(Pointer pvInstance) {
    super(pvInstance);
  }
  //10 = EnumPins
  public int EnumPins(PointerByReference ref) {
    return invokeInt(7, new Object[] { getPointer(), ref });
  }

  //11 = FindPin
  //12 = QueryFilterInfo
  //13 = JoinFilterGraph
  //14 = QueryVendorInfo
}
