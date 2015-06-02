package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;

public class IMediaFilter extends IPersist {
  public static class ByReference extends IMediaFilter implements Structure.ByReference {}
  public IMediaFilter() {}
  public IMediaFilter(Pointer pvInstance) {
    super(pvInstance);
  }
  //4 = Stop
  //5 = Pause
  //6 = Run
  //7 = GetState
  //8 = SetSyncSource
  //9 = GetSyncSource
}
