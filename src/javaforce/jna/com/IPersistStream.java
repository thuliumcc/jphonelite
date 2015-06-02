/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaforce.jna.com;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 *
 * @author pquiring
 */
public class IPersistStream extends IPersist {
  public static class ByReference extends IPersistStream implements Structure.ByReference {}
  public IPersistStream() {}
  public IPersistStream(Pointer pvInstance) {
    super(pvInstance);
  }
  //4 = IsDirty
  //5 = Load
  //6 = Save
  //7 = GetSizeMax
}
