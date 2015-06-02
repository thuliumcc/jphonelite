package javaforce;

/**
 *
 * @author pquiring
 *
 * Created : Sept 17, 2013
 */

import java.util.*;

public class JFArrayShort {
  private short buf[];
  private int count;

  public JFArrayShort() {
    count = 0;
    buf = new short[0];
  }

  public int size() {
    return count;
  }

  public void clear() {
    count = 0;
    buf = new short[0];
  }

  public void append(short s) {
    int newcount = count + 1;
    if (newcount > buf.length) {
      buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
    }
    buf[count] = s;
    count = newcount;
  }

  public void append(short s[]) {
    int newcount = count + s.length;
    if (newcount > buf.length) {
      buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
    }
    System.arraycopy(s, 0, buf, count, s.length);
    count = newcount;
  }

  public short[] toArray() {
    return Arrays.copyOf(buf, count);
  }
}
