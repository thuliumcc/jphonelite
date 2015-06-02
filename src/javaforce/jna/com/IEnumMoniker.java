package javaforce.jna.com;

import com.sun.jna.*;
import com.sun.jna.ptr.*;

/**
 *
 * @author pquiring
 */
public class IEnumMoniker extends IUnknown {
  public static class ByReference extends IEnumMoniker implements Structure.ByReference {}
  public IEnumMoniker() {}
  public IEnumMoniker(Pointer pvInstance) {
    super(pvInstance);
  }
  //4 methods (3-6)
  public int Next(int celt, PointerByReference rgelt, IntByReference pint) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(3 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, celt, rgelt, pint });
  }
  public int Skip(int celt) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(4 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, celt });
  }
  public int Reset() {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(5 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this });
  }
  public int Clone(PointerByReference ienummoniker) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(6 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, ienummoniker });
  }
}
