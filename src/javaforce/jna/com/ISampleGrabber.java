package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

public class ISampleGrabber extends IUnknown {
  public static class ByReference extends ISampleGrabber implements Structure.ByReference {}
  public ISampleGrabber() {}
  public ISampleGrabber(Pointer pvInstance) {
    super(pvInstance);
  }
  //3 = SetOneShot
  public int SetOneShot(boolean state) {
    return invokeInt(3, new Object[] { getPointer(), state });
  }
  //4 = SetMediaType
  public int SetMediaType(Pointer am_media_type) {
    return invokeInt(4, new Object[] { getPointer(), am_media_type });
  }
  //5 = GetConnectedMediaType
  //6 = SetBufferSamples
  public int SetBufferSamples(boolean state) {
    return invokeInt(6, new Object[] { getPointer(), state });
  }
  //7 = GetCurrentBuffer
  public int GetCurrentBuffer(IntByReference size, Pointer buffer) {
    return invokeInt(7, new Object[] { getPointer(), size, buffer });
  }
  //8 = GetCurrentSample
  //9 = SetCallback
}
