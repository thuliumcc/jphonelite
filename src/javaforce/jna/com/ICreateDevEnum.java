package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 17, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import javaforce.jna.com.Guid.*;

public class ICreateDevEnum extends IUnknown {
  public static class ByReference extends ICreateDevEnum implements Structure.ByReference {}
  public ICreateDevEnum() {}
  public ICreateDevEnum(Pointer pvInstance) {
    super(pvInstance);
  }
  public int CreateClassEnumerator(CLSID clsid, PointerByReference ienummoniker, int flags) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(3 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, clsid, ienummoniker, flags });
  }
}
