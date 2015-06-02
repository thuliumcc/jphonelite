package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 17, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.*;

public interface OleAut extends StdCallLibrary {
  public int VariantInit(Variant pVar);
  public int VariantClear(Variant pVar);
}
