package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;

import java.util.*;

public class VIDEOINFOHEADER extends Structure {
  /*
typedef struct tagVIDEOINFOHEADER {
    RECT rcSource;
    RECT rcTarget;
    DWORD dwBitRate;
    DWORD dwBitErrorRate;
    REFERENCE_TIME AvgTimePerFrame;
    BITMAPINFOHEADER bmiHeader;
} VIDEOINFOHEADER;
   */
  public RECT rcSource;
  public RECT rcTarget;
  public int dwBitRate;
  public int dwBitErrorRate;
  public long AvgTimePerFrame;
  public BITMAPINFOHEADER bmiHeader;

  @Override
  protected List getFieldOrder() {
    return Arrays.asList(new String[] {"rcSource", "rcTarget", "dwBitRate", "dwBitErrorRate"
      , "AvgTimePerFrame", "bmiHeader"});
  }

  public VIDEOINFOHEADER(Pointer p) {
    super(p);
  }
}
