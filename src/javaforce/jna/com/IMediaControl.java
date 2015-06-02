package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 18, 2013
 */

import com.sun.jna.*;

public class IMediaControl extends IDispatch {
  public static class ByReference extends IMediaControl implements Structure.ByReference {}
  public IMediaControl() {}
  public IMediaControl(Pointer pvInstance) {
    super(pvInstance);
  }
  //7 = Run
  public int Run() {
    return invokeInt(7, new Object[] { getPointer() });
  }
  //8 = Pause
  //9 = Stop
  public int Stop() {
    return invokeInt(9, new Object[] { getPointer() });
  }
  //10 = GetState
  //11 = RenderFile
  //12 = AddSourceFilter
  //13 = get_FilterCollection
  //14 = get_RegFilterCollection
  //15 = StopWhenReady
}
