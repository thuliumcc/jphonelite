package javaforce.jna.com;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 *
 * @author pquiring
 */
public class IPersist extends IUnknown {
  public static class ByReference extends IPersist implements Structure.ByReference {}
  public IPersist() {}
  public IPersist(Pointer pvInstance) {
    super(pvInstance);
  }
  //3 = GetClassID
}
