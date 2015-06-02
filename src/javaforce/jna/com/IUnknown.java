package javaforce.jna.com;

/**
 * IUnknown interface
 *
 * @author pquiring
 *
 * Created : Aug 17, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import javaforce.jna.com.Guid.*;

public class IUnknown extends PointerType {
  public static class ByReference extends IUnknown implements Structure.ByReference {}
  public IUnknown() {}
  public IUnknown(Pointer pvInstance) {
    super(pvInstance);
  }

  public int invokeInt(int idx, Object args[]) {
    //cpp_this = getPointer()
    //vtbl = cpp_this.getPointer();
    //funcPtr = vtbl.getPointer(idx * Pointer.SIZE)
    return Function.getFunction(getPointer().getPointer(0).getPointer(idx * Pointer.SIZE)).invokeInt(args);
  }

  public int QueryInterface(IID riid, PointerByReference ppvObject) {
    return invokeInt(0, new Object[] { getPointer(), riid, ppvObject });
  }
  public int AddRef() {
    return invokeInt(1, new Object[] { getPointer() });
  }
  public int Release() {
    return invokeInt(2, new Object[] { getPointer() });
  }
}
