package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;

public class IDispatch extends IUnknown {
  public static class ByReference extends IDispatch implements Structure.ByReference {}
  public IDispatch() {}
  public IDispatch(Pointer pvInstance) {
    super(pvInstance);
  }
  //3 = GetTypeInfoCount
  //4 = GetTypeInfo
  //5 = GetIDsOfNames
  //6 = Invoke
}
