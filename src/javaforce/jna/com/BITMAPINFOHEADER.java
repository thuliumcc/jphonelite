package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;

import java.util.*;

public class BITMAPINFOHEADER extends Structure {
/*
typedef struct tagBITMAPINFOHEADER {
  DWORD biSize;
  LONG  biWidth;
  LONG  biHeight;
  WORD  biPlanes;
  WORD  biBitCount;
  DWORD biCompression;
  DWORD biSizeImage;
  LONG  biXPelsPerMeter;
  LONG  biYPelsPerMeter;
  DWORD biClrUsed;
  DWORD biClrImportant;
} BITMAPINFOHEADER, *PBITMAPINFOHEADER;
 */
  public int biSize;
  public int biWidth;
  public int biHeight;
  public short biPlanes;
  public short biBitCount;
  public int biCompression;
  public int biSizeImage;
  public int biXPelsPerMeter;
  public int biYPelsPerMeter;
  public int biClrUsed;
  public int biClrImportant;

  @Override
  protected List getFieldOrder() {
    return Arrays.asList(new String[] {
      "biSize", "biWidth", "biHeight", "biPlanes", "biBitCount"
      , "biCompression", "biSizeImage", "biXPelsPerMeter", "biYPelsPerMeter"
      , "biClrUsed", "biClrImportant"
    });
  }

  public BITMAPINFOHEADER(Pointer p) {
    super(p);
  }
}
