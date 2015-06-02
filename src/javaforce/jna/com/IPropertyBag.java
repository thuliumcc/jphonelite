package javaforce.jna.com;

import com.sun.jna.*;

/**
 *
 * @author pquiring
 */
public class IPropertyBag extends IUnknown {
  public static class ByReference extends IPropertyBag implements Structure.ByReference {}
  public IPropertyBag() {}
  public IPropertyBag(Pointer pvInstance) {
    super(pvInstance);
  }
  public int Read(WString name, Variant var, Pointer iErrorLog) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(3 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, name, var, iErrorLog });
  }
  public int Write(WString name, Variant var) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(3 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, name, var });
  }

}
