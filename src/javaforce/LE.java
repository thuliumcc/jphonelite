package javaforce;

/**
 *  Little Endian get/set functions.
 *
 *  Usage : Windows files, Intel CPU style, etc.
 *
 *  Could use java.nio.ByteBuffer but sometimes this is faster.
 *
 *  Created : Dec 5, 2013
 */

public class LE {

  public static int getuint8(byte[] data, int offset) {
    int ret;
    ret  = (int)data[offset] & 0xff;
    return ret;
  }

  public static int getuint16(byte[] data, int offset) {
    int ret;
    ret  = (int)data[offset] & 0xff;
    ret += ((int)data[offset+1] & 0xff) << 8;
    return ret;
  }

  public static int getuint32(byte[] data, int offset) {
    int ret;
    ret  = (int)data[offset] & 0xff;
    ret += ((int)data[offset+1] & 0xff) << 8;
    ret += ((int)data[offset+2] & 0xff) << 16;
    ret += ((int)data[offset+3] & 0xff) << 24;
    return ret;
  }

  public static long getuint64(byte[] data, int offset) {
    long ret;
    ret  = (long)data[offset] & 0xff;
    ret += ((long)data[offset+1] & 0xff) << 8;
    ret += ((long)data[offset+2] & 0xff) << 16;
    ret += ((long)data[offset+3] & 0xff) << 24;
    ret += ((long)data[offset+4] & 0xff) << 32;
    ret += ((long)data[offset+5] & 0xff) << 40;
    ret += ((long)data[offset+6] & 0xff) << 48;
    ret += ((long)data[offset+7] & 0xff) << 56;
    return ret;
  }

  public static String getString(byte[] data, int offset, int len) {
    String ret = "";
    while (len > 0) {
      if (data[offset]==0) break;
      ret += (char)data[offset++];
      len--;
    }
    return ret;
  }

  public static void setuint8(byte[] data, int offset, int num) {
    data[offset] = (byte)(num & 0xff);
  }

  public static void setuint16(byte[] data, int offset, int num) {
    data[offset] = (byte)(num & 0xff);
    data[offset+1] = (byte)((num & 0xff00) >> 8);
  }

  public static void setuint32(byte[] data, int offset, int num) {
    data[offset] = (byte)(num & 0xff);
    data[offset+1] = (byte)((num & 0xff00) >> 8);
    data[offset+2] = (byte)((num & 0xff0000) >> 16);
    data[offset+3] = (byte)((num & 0xff000000) >> 24);
  }

  public static void setuint64(byte[] data, int offset, long num) {
    data[offset] = (byte)(num & 0xff);
    data[offset+1] = (byte)((num & 0xff00) >> 8);
    data[offset+2] = (byte)((num & 0xff0000) >> 16);
    data[offset+3] = (byte)((num & 0xff000000) >> 24);
    data[offset+4] = (byte)((num & 0xff00000000L) >> 32);
    data[offset+5] = (byte)((num & 0xff0000000000L) >> 40);
    data[offset+6] = (byte)((num & 0xff000000000000L) >> 48);
    data[offset+7] = (byte)((num & 0xff00000000000000L) >> 56);
  }

  public static void setString(byte[] data, int offset, int len, String str) {
    int pos = 0;
    while (len > 0) {
      if (pos >= str.length()) {
        data[offset++] = 0;
      } else {
        data[offset++] = (byte)str.charAt(pos++);
      }
      len--;
    }
  }
}
