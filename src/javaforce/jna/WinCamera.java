package javaforce.jna;

/**
 *
 * @author pquiring
 *
 * Created : Aug 16, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.*;

import javaforce.*;
import javaforce.media.*;
import javaforce.jna.com.*;

import java.util.*;

public class WinCamera implements Camera.Input {
  private static Ole ole;
  private static OleAut oleaut;

  private ArrayList<IMoniker> devices = new ArrayList<IMoniker>();
  private ArrayList<String> deviceNames = new ArrayList<String>();

  private static Pointer malloc(int size) {
    return new Pointer(Native.malloc(size));
  }

  private static void free(Pointer ptr) {
    Native.free(Pointer.nativeValue(ptr));
  }

  public boolean init() {
    try {
      if (ole == null) {
        ole = (Ole)Native.loadLibrary("ole32", Ole.class);
        if (ole == null) return false;
      }
      if (oleaut == null) {
        oleaut = (OleAut)Native.loadLibrary("oleaut32", OleAut.class);
        if (oleaut == null) return false;
      }
      return comInit();
    } catch (Throwable t) {
      JFLog.log(t);
    }
    return false;
  }

  public boolean uninit() {
    releaseAll();
    return comUnInit();
  }

  private static int comInitCount = 0;
  private static boolean comInit() {
    if (comInitCount == 0) {
      int res = ole.CoInitializeEx(null, ole.COINIT_MULTITHREADED);
      if (res != 0) return false;
    }
    comInitCount++;
    return true;
  }
  private static boolean comUnInit() {
    if (comInitCount > 0) comInitCount--;
    if (comInitCount == 0) {
      ole.CoUninitialize();
    }
    return true;
  }

  /** List available devices */
  public String[] listDevices() {
    while (devices.size() > 0) {
      IMoniker moniker = devices.remove(0);
      moniker.Release();
      moniker = null;
    }
    deviceNames.clear();

    try {
      ICreateDevEnum createDevEnum;
      IEnumMoniker enumMoniker;
      IMoniker moniker;
      IPropertyBag propBag;

      PointerByReference ref = new PointerByReference();

      int res = ole.CoCreateInstance(Guid.CLSID_SystemDeviceEnum, null, Ole.CLSCTX_INPROC_SERVER
        , Guid.IID_ICreateDevEnum, ref);
      if (res != 0) throw new Exception("CoCreateInstance failed!");
      createDevEnum = new ICreateDevEnum(ref.getValue());
//      System.out.println("ICreateDevEnum=" + createDevEnum);

      ref = new PointerByReference();
      res = createDevEnum.CreateClassEnumerator(Guid.CLSID_VideoInputDeviceCategory, ref, 0);
      if (res != 0) throw new Exception("ICreateDevEnum.CreateClassEnumerator failed!");
      enumMoniker = new IEnumMoniker(ref.getValue());
//      System.out.println("IEnumMoniker=" + enumMoniker);
      while(true) {
        ref = new PointerByReference();
        enumMoniker.Next(1, ref, null);
        if (ref.getValue() == null) break;
        moniker = new IMoniker(ref.getValue());
//        System.out.println("IMoniker=" + moniker);
        res = moniker.BindToStorage(null, null, Guid.IID_IPropertyBag, ref);
        if (res != 0) {
//          System.out.println("IMoniker.BindToStorage failed");
          moniker.Release();
          moniker = null;
          continue;  //this one failed, try the next one
        }
        propBag = new IPropertyBag(ref.getValue());
//        System.out.println("IPropertyBag=" + propBag);
        Variant var = new Variant();
        oleaut.VariantInit(var);
        res = propBag.Read(new WString("Description"), var, null);
        if (res != 0) res = propBag.Read(new WString("FriendlyName"), var, null);
        if (res == 0) {
          Pointer varData = (Pointer)var.data.getTypedValue(Pointer.class);
          deviceNames.add(varData.getWideString(0));
          devices.add(moniker);
        } else {
//          System.out.println("IPropertyBag.Read failed");
          moniker.Release();
          moniker = null;
        }
        oleaut.VariantClear(var);
        propBag.Release();
        propBag = null;
      }
      enumMoniker.Release();
      enumMoniker = null;
      createDevEnum.Release();
      createDevEnum = null;
    } catch (Exception e) {
      JFLog.log(e);
    }
    return deviceNames.toArray(new String[0]);
  }

  private ICaptureGraphBuilder2 captureGraphBuilder;
  private IGraphBuilder graphBuilder;
  private IMediaControl mediaControl;
  private IBaseFilter videoInputFilter;
  private IAMStreamConfig streamConfig;
  private AM_MEDIA_TYPE mediaType;
  private AM_MEDIA_TYPE enumMediaType;
  private AM_MEDIA_TYPE rgbMediaType;
  private VIDEOINFOHEADER videoInfo;
  private IBaseFilter sampleGrabberBaseFilter;
  private ISampleGrabber sampleGrabber;
  private IBaseFilter destBaseFilter;  //NULL dest
  private Guid.GUID PIN;
  private int width, height;
  private Pointer buffer;
  private int px[], pxf[];
  private byte rgb[];
  private IPin pin;
  private IEnumMediaTypes enumMediaTypes;

  public boolean start(int deviceIdx, int desiredWidth, int desiredHeight) {
    if (deviceIdx >= devices.size()) return false;
    try {
      PointerByReference ref = new PointerByReference();
      int res = ole.CoCreateInstance(Guid.CLSID_CaptureGraphBuilder2, null, Ole.CLSCTX_INPROC_SERVER
        ,Guid.IID_ICaptureGraphBuilder2 , ref);
      if (res != 0) throw new Exception("CoCreateInstance() failed");
      captureGraphBuilder = new ICaptureGraphBuilder2(ref.getValue());

//      System.out.println("cg=" + captureGraphBuilder);

      ref = new PointerByReference();
      res = ole.CoCreateInstance(Guid.CLSID_FilterGraph, null, Ole.CLSCTX_INPROC_SERVER
        ,Guid.IID_IGraphBuilder, ref);
      if (res != 0) throw new Exception("CoCreateInstance() failed");
      graphBuilder = new IGraphBuilder(ref.getValue());

      res = captureGraphBuilder.SetFiltergraph(graphBuilder.getPointer());
      if (res != 0) throw new Exception("ICaptureGraphBuilder2.SetFiltergraph() failed");

      ref = new PointerByReference();
      res = graphBuilder.QueryInterface(Guid.IID_IMediaControl, ref);
      if (res != 0) throw new Exception("IGraphBuilder.QueryInterface() failed");
      mediaControl = new IMediaControl(ref.getValue());

      IMoniker moniker = devices.get(deviceIdx);

      ref = new PointerByReference();
      res = moniker.BindToObject(null, null, Guid.IID_IBaseFilter, ref);
      if (res != 0) throw new Exception("IMoniker.BindToObject() failed");
      videoInputFilter = new IBaseFilter(ref.getValue());

//      System.out.println("vif=" + videoInputFilter);

      graphBuilder.AddFilter(videoInputFilter.getPointer(), new WString(deviceNames.get(deviceIdx)));

      ref = new PointerByReference();
      res = captureGraphBuilder.FindInterface(Guid.PIN_CATEGORY_PREVIEW, Guid.MEDIATYPE_Video, videoInputFilter.getPointer()
        ,Guid.IID_IAMStreamConfig, ref);
      if (res != 0) {
        System.out.println("Warning:Couldn't find preview pin using SmartTee");
        ref = new PointerByReference();
        res = captureGraphBuilder.FindInterface(Guid.PIN_CATEGORY_CAPTURE, Guid.MEDIATYPE_Video, videoInputFilter.getPointer()
          ,Guid.IID_IAMStreamConfig, ref);
        if (res != 0) throw new Exception("ICaptureGraphBuilder2.FindInterface failed()");
        PIN = Guid.PIN_CATEGORY_CAPTURE;
      } else {
        PIN = Guid.PIN_CATEGORY_PREVIEW;
      }
      streamConfig = new IAMStreamConfig(ref.getValue());

//      System.out.println("sc=" + streamConfig);

      //TODO : routeCrossbar() : needed???

      ref = new PointerByReference();
      res = captureGraphBuilder.FindInterface(PIN, Guid.MEDIATYPE_Video, videoInputFilter.getPointer()
        ,Guid.IID_IPin, ref);
      if (res != 0) throw new Exception("ICaptureGraphBuilder2.FindInterface() failed");
      pin = new IPin(ref.getValue());

      ref = new PointerByReference();
      res = pin.EnumMediaTypes(ref);
      if (res != 0) throw new Exception("IPin.EnumMediaTypes() failed");
      enumMediaTypes = new IEnumMediaTypes(ref.getValue());

/*
      //enumerate pins (not needed)
      ref = new PointerByReference();
      res = videoInputFilter.EnumPins(ref);
      if (res != 0) throw new Exception("IBaseFilter.EnumPins() failed");
*/

      ref = new PointerByReference();
      res = streamConfig.GetFormat(ref);
      if (res != 0) throw new Exception("IAMStreamConfig.GetFormat() failed");
      mediaType = new AM_MEDIA_TYPE(ref.getValue());
      mediaType.read();

      videoInfo = new VIDEOINFOHEADER(mediaType.pbFormat);
      videoInfo.read();
      width = videoInfo.bmiHeader.biWidth;
      height = videoInfo.bmiHeader.biHeight;

/*
      //TODO : try to for a different size
      if ((desiredWidth == -1) || (desiredHeight == -1)) {
        desiredWidth = width;
        desiredHeight = height;
      }

      if (desiredWidth != width || desiredHeight != height) {
        //enumerate formats
        boolean ok = false;
        while (true) {
          ref = new PointerByReference();
          res = enumMediaTypes.Next(1, ref, null);
          if (res != 0) break;
          enumMediaType = new AM_MEDIA_TYPE(ref.getValue());
          enumMediaType.read();
//          System.out.println("enumMediaType=" + mediaType);
          videoInfo = new VIDEOINFOHEADER(mediaType.pbFormat);
          videoInfo.read();
          int enumWidth = videoInfo.bmiHeader.biWidth;
          int enumHeight = videoInfo.bmiHeader.biHeight;
          System.out.println("size=" + enumWidth + "," + enumHeight);
          if (enumWidth == desiredWidth && enumHeight == desiredHeight) {
            width = enumWidth;
            height = enumHeight;
            //TODO : streamConfig.setFormat(enumMediaType);
          }
        }
      }
*/

      streamConfig.Release();
      streamConfig = null;

//      System.out.println("mt=" + mediaType);
      if (mediaType.pbFormat == null) throw new Exception("IAMStreamConfig.GetFormat() returned null pbFormat");

      videoInfo = new VIDEOINFOHEADER(mediaType.pbFormat);
      videoInfo.read();
      width = videoInfo.bmiHeader.biWidth;
      height = videoInfo.bmiHeader.biHeight;

//      System.out.println("vi=" + videoInfo);

      ref = new PointerByReference();
      res = ole.CoCreateInstance(Guid.CLSID_SampleGrabber, null, Ole.CLSCTX_INPROC_SERVER
        ,Guid.IID_IBaseFilter, ref);
      if (res != 0) throw new Exception("CoCreateInstance() failed");
      sampleGrabberBaseFilter = new IBaseFilter(ref.getValue());

      res = graphBuilder.AddFilter(sampleGrabberBaseFilter.getPointer(),new WString("Sample Grabber"));
      if (res != 0) throw new Exception("IGraphBuilder.AddFilter() failed");

      ref = new PointerByReference();
      res = sampleGrabberBaseFilter.QueryInterface(Guid.IID_ISampleGrabber, ref);
      if (res != 0) throw new Exception("IBaseFilter.QueryInterface() failed");
      sampleGrabber = new ISampleGrabber(ref.getValue());

      sampleGrabber.SetOneShot(false);
      sampleGrabber.SetBufferSamples(true);

      rgbMediaType = new AM_MEDIA_TYPE();
      rgbMediaType.majortype = Guid.MEDIATYPE_Video;
      rgbMediaType.subtype = Guid.MEDIASUBTYPE_RGB32;
      rgbMediaType.formattype = Guid.FORMAT_VideoInfo;
      rgbMediaType.write();
      sampleGrabber.SetMediaType(rgbMediaType.getPointer());

      ref = new PointerByReference();
      res = ole.CoCreateInstance(Guid.CLSID_NullRenderer, null, Ole.CLSCTX_INPROC_SERVER
        ,Guid.IID_IBaseFilter, ref);
      if (res != 0) throw new Exception("CoCreateInstance() failed");
      destBaseFilter = new IBaseFilter(ref.getValue());

      res = graphBuilder.AddFilter(destBaseFilter.getPointer(), new WString("NullRenderer"));
      if (res != 0) throw new Exception("IGraphBuilder.AddFilter() failed");

      res = captureGraphBuilder.RenderStream(PIN, Guid.MEDIATYPE_Video
        , videoInputFilter.getPointer(), sampleGrabberBaseFilter.getPointer(), destBaseFilter.getPointer());
      if (res != 0) throw new Exception("ICaptureGraphBuilder2.RenderStream() failed");

      res = mediaControl.Run();
      if (res == 0x1) res = 0;  //S_FALSE = preparing to run but not ready yet
      if (res != 0) throw new Exception("IMediaControl.Run() failed:" + Integer.toString(res, 16));

      buffer = malloc(width * height * 4);
      px = new int[width * height];
      pxf = new int[width * height];
      rgb = new byte[width * height * 4];

      videoInputFilter.Release();
      videoInputFilter = null;
      sampleGrabberBaseFilter.Release();
      sampleGrabberBaseFilter = null;
      destBaseFilter.Release();
      destBaseFilter = null;

    } catch (Exception e) {
      JFLog.log(e);
      releaseAll();
      return false;
    }
    return true;
  }

  public int[] getFrame() {
    if (sampleGrabber == null) return null;
    IntByReference intref = new IntByReference();
    intref.setValue(width * height * 4);

    int res = sampleGrabber.GetCurrentBuffer(intref, buffer);
    if (res != 0) return null;
//    System.out.println("bufferSize=" + intref.getValue());

    buffer.read(0, px, 0, width * height);

    //copy pixels, flip image, set opaque alpha channel
    int dst = 0;
    int src = width * (height-1);
    int w2 = width * 2;
    for(int y=0;y<height;y++) {
      for(int x=0;x<width;x++) {
        pxf[dst++] = px[src++] | 0xff000000;
      }
      src -= w2;
    }

//    System.out.println(String.format("samples=%x,%x,%x",px[100],px[101],px[102]));

    return pxf;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public boolean stop() {
    if (mediaControl != null) {
      mediaControl.Stop();
    }
    return true;
  }

  private void releaseAll() {
    if (captureGraphBuilder != null) {
      captureGraphBuilder.Release();
      captureGraphBuilder = null;
    }
    if (graphBuilder != null) {
      graphBuilder.Release();
      graphBuilder = null;
    }
    if (mediaControl != null) {
      mediaControl.Release();
      mediaControl = null;
    }
    if (videoInputFilter != null) {
      videoInputFilter.Release();
      videoInputFilter = null;
    }
    if (streamConfig != null) {
      streamConfig.Release();
      streamConfig = null;
    }
    if (buffer != null) {
      free(buffer);
      buffer = null;
    }
    px = null;
    rgb = null;
  }
}
