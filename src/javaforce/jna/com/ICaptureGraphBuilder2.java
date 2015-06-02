package javaforce.jna.com;

/**
 *
 * @author pquiring
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import javaforce.jna.com.Guid.*;

public class ICaptureGraphBuilder2 extends IUnknown {
  public static class ByReference extends ICaptureGraphBuilder2 implements Structure.ByReference {}
  public ICaptureGraphBuilder2() {}
  public ICaptureGraphBuilder2(Pointer pvInstance) {
    super(pvInstance);
  }
  //3 = SetFiltergraph
  public int SetFiltergraph(Pointer iGraph) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(3 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, iGraph });
  }
  //4 = GetFiltergraph
  //5 = SetOutputFileName
  //6 = FindInterface
  public int FindInterface(GUID category, GUID type, Pointer iBaseFilter, IID refiid, PointerByReference ppint) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(6 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, category, type, iBaseFilter, refiid, ppint });
  }
  //7 = RenderStream
  public int RenderStream(GUID category, GUID type, Pointer src, Pointer compressor, Pointer renderer) {
    return invokeInt(7, new Object[] { getPointer(), category, type, src, compressor, renderer});
  }
  //8 = ControlStream
  //9 = AllocCapFile
  //10 = CopyCaptureFile
  //11 = FindPin
}
