package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;

import java.util.*;

public class AM_MEDIA_TYPE extends Structure {
/*
typedef struct _AMMediaType {
  GUID majortype;
  GUID subtype;
  WINBOOL bFixedSizeSamples;
  WINBOOL bTemporalCompression;
  ULONG lSampleSize;
  GUID formattype;
  IUnknown *pUnk;
  ULONG cbFormat;
  BYTE *pbFormat;
} AM_MEDIA_TYPE;
*/
  public Guid.GUID majortype;
  public Guid.GUID subtype;
  public int bFixedSizeSamples;  //BOOL
  public int bTemporalCompression;  //BOOL
  public int lSampleSize;
  public Guid.GUID formattype;
  public Pointer pUnk;
  public int cbFormat;
  public Pointer pbFormat;

  @Override
  protected List getFieldOrder() {
    return Arrays.asList(new String[] {"majortype", "subtype", "bFixedSizeSamples", "bTemporalCompression"
      , "lSampleSize", "formattype", "pUnk", "cbFormat", "pbFormat"});
  }

  public AM_MEDIA_TYPE(Pointer p) {
    super(p);
  }
  public AM_MEDIA_TYPE() {
  }
}
