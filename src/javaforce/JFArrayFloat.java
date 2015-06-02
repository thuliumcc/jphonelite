package javaforce;

/**
 *
 * @author pquiring
 *
 * Created : Sept 17, 2013
 */

import java.util.*;

public class JFArrayFloat {
  private float buf[];
  private int count;

  public JFArrayFloat() {
    count = 0;
    buf = new float[0];
  }

  public int size() {
    return count;
  }

  public void clear() {
    count = 0;
    buf = new float[0];
  }

  public void append(float f) {
    int newcount = count + 1;
    if (newcount > buf.length) {
      buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
    }
    buf[count] = f;
    count = newcount;
  }

  public void append(float f[]) {
    int newcount = count + f.length;
    if (newcount > buf.length) {
      buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
    }
    System.arraycopy(f, 0, buf, count, f.length);
    count = newcount;
  }

  public float[] toArray() {
    return Arrays.copyOf(buf, count);
  }
}
