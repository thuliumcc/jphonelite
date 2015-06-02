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

public class IMoniker extends IPersistStream {
  public static class ByReference extends IMoniker implements Structure.ByReference {}
  public IMoniker() {}
  public IMoniker(Pointer pvInstance) {
    super(pvInstance);
  }
  //15 methods (8-22)
  //8 = BindToObject
  public int BindToObject(Pointer iBindCtx, Pointer iMoniker, IID refiid, PointerByReference pvResult) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(8 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, iBindCtx, iMoniker, refiid, pvResult });
  }
  //9 = BindToStorage
  public int BindToStorage(Pointer iBindCtx, Pointer iMoniker, IID refiid, PointerByReference ppv) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(9 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, iBindCtx, iMoniker, refiid, ppv });
  }
  //10 = Reduce
  //11 = ComposeWith
  //12 = Enum
  //13 = IsEqual
  //14 = Hash
  //15 = IsRunning
  //16 = GetTimeOfLastChange
  //17 = Inverse
  //18 = CommonPrefixWith
  //19 = RelativePathTo
  //20 = GetDisplayName
  //21 = ParseDisplayName
  //22 = IsSystemMoniker
}
