package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 17, 2013
 */

import com.sun.jna.*;

import java.util.*;

public class Variant extends Structure {
  public short type;
  public short r2, r3, r4;
  public static class Data extends Union {
    public byte i8;
    public short i16;
    public int i32;
    public long i64;
    public Pointer string;
    public Pointer ptr;
  }
  public Data data;
  @Override
  protected List getFieldOrder() {
    return Arrays.asList(new String[] {"type", "r2", "r3", "r4", "data"});
  }
}
