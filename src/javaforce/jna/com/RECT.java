package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;

import java.util.*;

public class RECT extends Structure {
  public int left, top, right, bottom;

  @Override
  protected List getFieldOrder() {
    return Arrays.asList(new String[] {"left", "top", "right", "bottom"});
  }

  public RECT() {
  }
  public RECT(Pointer p) {
    super(p);
  }
}
