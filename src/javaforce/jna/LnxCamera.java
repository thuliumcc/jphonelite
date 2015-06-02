package javaforce.jna;

/**
 * Video4Linux2 (JNA)
 *
 * This code only supports the following pixel formats: YUYV, MJPEG and RGB32.
 * Recommend using ffmpeg libavdevice instead.
 *
 * @author pquiring
 *
 * Created : Sept 9, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import java.io.*;
import java.util.*;

import javaforce.*;
import javaforce.media.*;

public class LnxCamera implements Camera.Input {
  private static Pointer malloc(int size) {
    return new Pointer(Native.malloc(size));
  }

  private static void free(Pointer ptr) {
    Native.free(Pointer.nativeValue(ptr));
  }
  public static interface C extends Library {
    public int open(String name, int flags);
    public int close(int fd);
    public int ioctl(int fd, int req, Pointer arg);
    //for reading API
    public int read(int fd, Pointer buf, int len);
    //for streaming API
    public Pointer mmap(Pointer start, int length, int prot, int flags, int fd, Pointer offset);
    public void munmap(Pointer start, int length);
  }
  public static class v4l2_capabilities extends Structure {
    public byte driver[] = new byte[16];
    public byte card[] = new byte[32];
    public byte bus_info[] = new byte[32];
    public int version;
    public int capabilities;
    public int device_caps;
    public int res[] = new int[3];

    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {"driver", "card", "bus_info", "version", "capabilities"
        , "device_caps", "res"});
    }
  }
  public static class v4l2_cropcap extends Structure {
    public int type;
    public int bounds_left;  //_rect
    public int bounds_top;  //_rect
    public int bounds_width;  //_rect
    public int bounds_height;  //_rect
    public int defrect_left;  //_rect
    public int defrect_top;  //_rect
    public int defrect_width;  //_rect
    public int defrect_height;  //_rect
    public int pixelaspect_num;  //_fract
    public int pixelaspect_den;  //_fract
    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {
        "type", "bounds_left", "bounds_top", "bounds_width", "bounds_height", "defrect_left",
        "defrect_top", "defrect_width", "defrect_height", "pixelaspect_num", "pixelaspect_den"
      });
    }
  }
  public static class v4l2_crop extends Structure {
    public int type;
    public int c_left;  //_rect
    public int c_top;  //_rect
    public int c_width;  //_rect
    public int c_height;  //_rect
    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {
        "type", "c_left", "c_top", "c_width", "c_height"
      });
    }
  }
  public static class v4l2_format extends Structure {  //pix_format type
    public int type;
    //class v4l2_pix_format {
    public int width, height, pixelformat, field, bytesperline, sizeimage, colorspace, priv;
    //}pix;
    public byte fluff[] = new byte[256];
    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {
        "type", "width", "height", "pixelformat", "field", "bytesperline", "sizeimage", "colorspace", "priv", "fluff"
      });
    }
  }
  public static class v4l2_requestbuffers extends Structure {
    public int count, type, memory, res1, res2;
    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {
        "count", "type", "memory", "res1", "res2"
      });
    }
  }
  public static class v4l2_buffer extends Structure {
    public int index, type, bytesused, flags, field;
    public int timestamp_tv_sec;
    public int timestamp_tv_usec;
    public byte timecode[] = new byte[16];
    public int sequence, memory;
    //union {
    public Pointer offset;  //32 or 64bits
    //} m;
    public int length, res1, res2;
    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {
        "index", "type", "bytesused", "flags", "field", "timestamp_tv_sec", "timestamp_tv_usec", "timecode",
        "sequence", "memory", "offset", "length", "res1", "res2"
      });
    }
  }
  public static class v4l2_fmtdesc extends Structure {
    public int index, type, flags;
    public byte desc[] = new byte[32];
    public int pixelformat, res[] = new int[4];
    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {
        "index", "type", "flags", "desc", "pixelformat", "res"
      });
    }
  }
  public static class v4l2_frmsizeenum extends Structure {
    public int index, pixelformat, type;
    public int width, height;  //discrete (stepwise: min_width , max_width)
    public int step_width;  //stepwise
    public int min_height, max_height, step_height;  //stepwise
    public int res[] = new int[2];
    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {
        "index", "pixelformat", "type", "width", "height", "step_width", "min_height", "max_height", "step_height", "res"
      });
    }
  }

  private static class mmapbuffer {
    public Pointer start;
    public int length;
  }
  private static C c;
  //constants (see /linux/videodev2.h)
  private static final int O_RDWR = 02;
  private static final int O_NONBLOCK = 04000;
  private static final int EINTR = 4;

  private static final int VIDIOC_QUERYCAP  = 0x80685600;  //'V', 0
  private static final int VIDIOC_ENUM_FMT  = 0xC0405602;  //'V', 2
  private static final int VIDIOC_G_FMT     = 0xC0CC5604;  //'V', 4
  private static final int VIDIOC_S_FMT     = 0xC0CC5605;  //'V', 5
  private static final int VIDIOC_REQBUFS   = 0xC0145608;  //'V', 8
  private static final int VIDIOC_QUERYBUF  = 0xC0445609;  //'V', 9
  private static final int VIDIOC_QBUF      = 0xC044560f;  //'V', 15
  private static final int VIDIOC_DQBUF     = 0xC0445611;  //'V', 17
  private static final int VIDIOC_STREAMON  = 0x40045612;  //'V', 18
  private static final int VIDIOC_STREAMOFF = 0x40045613;  //'V', 19
  private static final int VIDIOC_S_STD     = 0x40085618;  //'V', 24
  private static final int VIDIOC_ENUMSTD   = 0xC0405619;  //'V', 25
  private static final int VIDIOC_ENUMINPUT = 0xC04C561a;  //'V', 26
  private static final int VIDIOC_G_INPUT   = 0x80045626;  //'V', 38
  private static final int VIDIOC_S_INPUT   = 0xC0045627;  //'V', 39
  private static final int VIDIOC_CROPCAP   = 0xC02C563a;  //'V', 58
  private static final int VIDIOC_S_CROP    = 0x4014563c;  //'V', 60
  private static final int VIDIOC_ENUM_FRAMESIZES = 0xC02C564a;  //'V', 74

  private static final int V4L2_CAP_VIDEO_CAPTURE = 0x00000001;
  private static final int V4L2_CAP_READWRITE =     0x01000000;
  private static final int V4L2_CAP_STREAMING =     0x04000000;

  private static final int V4L2_BUF_TYPE_VIDEO_CAPTURE = 1;
  private static final int V4L2_BUF_TYPE_VIDEO_CAPTURE_MPLANE = 9;

  private static final int V4L2_PIX_FMT_YUV420 = 0x32315559;  //'Y', 'U', '1', '2'
  private static final int V4L2_PIX_FMT_YUYV = 0x56595559;  //'Y', 'U', 'Y', 'V' (YUV422)
  private static final int V4L2_PIX_FMT_MJPG = 0x47504a4d;  //'M', 'J', 'P', 'G'
  private static final int V4L2_PIX_FMT_BGR32 = 0x34524742;  //'B', 'G', 'R,' '4'
  private static final int V4L2_PIX_FMT_RGB32 = 0x34424752;  //'R', 'G', 'B,' '4'
  private static final int V4L2_PIX_FMT_RGB24 = 0x33424752;  //'R', 'G', 'B,' '3'

  private static final int V4L2_FIELD_INTERLACED = 4;

  private static final int V4L2_MEMORY_MMAP = 1;

  private static final int PROT_READ = 0x01;
  private static final int PROT_WRITE = 0x02;

  private static final int MAP_SHARED = 0x01;

  private static final int V4L2_BUF_FLAG_MAPPED = 0x01;
  private static final int V4L2_BUF_FLAG_QUEUED = 0x02;
  private static final int V4L2_BUF_FLAG_DONE   = 0x04;

  private static final int V4L2_FRMSIZE_TYPE_DISCRETE   = 1;
  private static final int V4L2_FRMSIZE_TYPE_CONTINUUOS = 2;
  private static final int V4L2_FRMSIZE_TYPE_STEPWISE   = 3;

  private ArrayList<String> devices = new ArrayList<String>();
  private int fd = -1, width, height, bytesperline, imagesize;
  private Pointer read_buffer = null;
  private mmapbuffer mmapbuffers[] = new mmapbuffer[2];
  private int mmapbuffers_type;
  private int px[];
  private int api = -1;  //V4L2_CAP_READWRITE OR STREAMING
  private int pixfmt;

  //a blocking ioctl
  private int xioctl(int fd, int req, Pointer arg) {
    int r;
    do {
      r = c.ioctl(fd, req, arg);
    } while (r == -1 && Native.getLastError() == EINTR);
    return r;
  }

  public boolean init() {
    try {
      if (c == null) {
        c = (C)Native.loadLibrary("c", C.class);
      }
    } catch (Throwable t) {
      JFLog.log(t);
      return false;
    }
    return true;
  }

  public boolean uninit() {
    return true;
  }

  public String[] listDevices() {
    devices.clear();
    for(int a=0;;a++) {
      String device = "/dev/video" + a;
      int fd = c.open(device, O_RDWR | O_NONBLOCK);
      if (fd == -1) break;
      c.close(fd);
      devices.add(device);
    }
    return devices.toArray(new String[0]);
  }

  public boolean start(int deviceIdx, int req_width, int req_height) {
    if (deviceIdx >= devices.size()) return false;
    Native.setLastError(0);
    fd = c.open("/dev/video" + deviceIdx, O_RDWR | O_NONBLOCK);
    if (fd == -1) {
      JFLog.log("LnxCamera:Failed to open camera");
      return false;
    }
    v4l2_capabilities caps = new v4l2_capabilities();
    caps.write();
    int res = xioctl(fd, VIDIOC_QUERYCAP, caps.getPointer());
    if (res == -1) {
      JFLog.log("LnxCamera:Failed to get camera caps:" + Native.getLastError());
      return false;
    }
    caps.read();
    if ((caps.capabilities & V4L2_CAP_VIDEO_CAPTURE) == 0) {
      JFLog.log("LnxCamera:Device is not a camera:" + Native.getLastError());
      return false;
    }
    api = caps.capabilities & V4L2_CAP_STREAMING;
    if (api == 0) {
      api = caps.capabilities & V4L2_CAP_READWRITE;
      if (api == 0) {
        JFLog.log("LnxCamera:Camera doesn't support read/write or streaming mode:" + Native.getLastError());
        return false;
      }
    }
    //enum formats
    pixfmt = -1;
    int pixfmtpri = -1;  //pixel format priority
    for(int fdi = 0;;fdi++) {
      v4l2_fmtdesc fmtdesc = new v4l2_fmtdesc();
      fmtdesc.index = fdi;
      fmtdesc.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
      fmtdesc.write();
      if (xioctl(fd, VIDIOC_ENUM_FMT, fmtdesc.getPointer()) == -1) {
        break;
      }
      fmtdesc.read();
      JFLog.log("LnxCamera:fmtdesc=" + String.format("%x", fmtdesc.pixelformat) + ":" + new String(fmtdesc.desc));
      switch (fmtdesc.pixelformat) {
        case V4L2_PIX_FMT_MJPG:  //my camera outputs half jpeg files
          if (pixfmtpri < 0x1) {pixfmt = fmtdesc.pixelformat; pixfmtpri = 0x1;}
          break;
        case V4L2_PIX_FMT_YUYV:
          if (pixfmtpri < 0x10) {pixfmt = fmtdesc.pixelformat; pixfmtpri = 0x10;}
          break;
        case V4L2_PIX_FMT_RGB32:
          if (pixfmtpri < 0x100) {pixfmt = fmtdesc.pixelformat; pixfmtpri = 0x100;}
          break;
      }
      //enum framesize (width/height)
      for(int fsi=0;;fsi++) {
        v4l2_frmsizeenum frmsize = new v4l2_frmsizeenum();
        frmsize.index = fsi;
        frmsize.pixelformat = fmtdesc.pixelformat;
        frmsize.write();
        if (xioctl(fd, VIDIOC_ENUM_FRAMESIZES, frmsize.getPointer()) == -1) {
          break;
        }
        frmsize.read();
        switch (frmsize.type) {
          case V4L2_FRMSIZE_TYPE_DISCRETE:
            JFLog.log("LnxCamera:size=" + frmsize.width + "x" + frmsize.height);
            break;
          case V4L2_FRMSIZE_TYPE_CONTINUUOS:
          case V4L2_FRMSIZE_TYPE_STEPWISE:
            JFLog.log("LnxCamera:size={" + frmsize.width + "-" + frmsize.height + "," + frmsize.step_width
              + "}x{" + frmsize.min_height + "-" + frmsize.max_height + "," + frmsize.step_height);
            break;
        }
      }
    }
    if (pixfmt == -1) {
      JFLog.log("LnxCamera:No supported pixelformat available");
      return false;
    }
    //set crop
    v4l2_cropcap cropcap = new v4l2_cropcap();
    cropcap.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    cropcap.write();
    xioctl(fd, VIDIOC_CROPCAP, cropcap.getPointer());  //ignore errors
    cropcap.read();
    v4l2_crop crop = new v4l2_crop();
    crop.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    crop.c_left = cropcap.defrect_left;
    crop.c_top = cropcap.defrect_top;
    crop.c_width = cropcap.defrect_width;
    crop.c_height = cropcap.defrect_height;
    crop.write();
    xioctl(fd, VIDIOC_S_CROP, crop.getPointer());  //ignore errors
    //set format
    v4l2_format fmt = new v4l2_format();
    fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    fmt.write();
    if (xioctl(fd, VIDIOC_G_FMT, fmt.getPointer()) == -1) {
      JFLog.log("LnxCamera:Failed to get video format:" + Native.getLastError());
      return false;
    }
    fmt.read();
    if (req_width == -1 || req_height == -1) {
      req_width = 640;
      req_height = 480;
    }
    fmt.width = req_width;
    fmt.height = req_height;
    fmt.pixelformat = pixfmt;
    fmt.field = V4L2_FIELD_INTERLACED;
    fmt.write();
    if (xioctl(fd, VIDIOC_S_FMT, fmt.getPointer()) == -1) {
      JFLog.log("LnxCamera:Failed to set video format:" + Native.getLastError());
      return false;
    }
    fmt.read();
    pixfmt = fmt.pixelformat;
    switch (pixfmt) {
      case V4L2_PIX_FMT_MJPG:
      case V4L2_PIX_FMT_YUYV:
      case V4L2_PIX_FMT_RGB32:
        JFLog.log("LnxCamera:selected pixelformat=" + String.format("%x", pixfmt));
        break;
      default:
        JFLog.log("LnxCamera:Unsuppported pixelformat:" + String.format("%x", pixfmt));
        return false;
    }
    //NOTE:width/height may have changed
    width = fmt.width;
    height = fmt.height;
    bytesperline = fmt.bytesperline;
    if (bytesperline < width) bytesperline = width;
    imagesize = fmt.sizeimage;
    if (imagesize < bytesperline * height) imagesize = bytesperline * height;

    switch (api) {
      case V4L2_CAP_READWRITE:
        //init reading
        read_buffer = malloc(imagesize);
        break;
      case V4L2_CAP_STREAMING:
        //init streaming
        v4l2_requestbuffers requestbuffers = new v4l2_requestbuffers();
        requestbuffers.count = 2;
        requestbuffers.memory = V4L2_MEMORY_MMAP;
        requestbuffers.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        requestbuffers.write();
        if (xioctl(fd, VIDIOC_REQBUFS, requestbuffers.getPointer()) == -1) {
          JFLog.log("LnxCamera:Failed to alloc MMAP Buffers");
          return false;
        }
        requestbuffers.read();
        if (requestbuffers.count != 2) {
          JFLog.log("LnxCamera:Failed to allocate mmap buffers");
          return false;
        }
        mmapbuffers_type = requestbuffers.type;
        v4l2_buffer buffer;
        for(int i=0;i<2;i++) {
          buffer = new v4l2_buffer();
          buffer.type = mmapbuffers_type;
          buffer.memory = V4L2_MEMORY_MMAP;
          buffer.index = i;
          buffer.write();
          if (xioctl(fd, VIDIOC_QUERYBUF, buffer.getPointer()) == -1) {
            JFLog.log("LnxCamera:Failed to query mmap buffer");
            return false;
          }
          buffer.read();
          mmapbuffers[i] = new mmapbuffer();
          mmapbuffers[i].length = buffer.length;
          mmapbuffers[i].start = c.mmap(null, buffer.length, PROT_READ | PROT_WRITE, MAP_SHARED, fd, buffer.offset);
          if (xioctl(fd, VIDIOC_QBUF, buffer.getPointer()) == -1) {
            JFLog.log("LnxCamera:Failed to queue buffer");
            return false;
          }
        }
        IntByReference type = new IntByReference();
        type.setValue(V4L2_BUF_TYPE_VIDEO_CAPTURE);
        if (xioctl(fd, VIDIOC_STREAMON, type.getPointer()) == -1) {
          JFLog.log("LnxCamera:Failed to start stream:" + Native.getLastError());
          return false;
        }
        break;
    }
    px = new int[width * height];
    return true;
  }

  public boolean stop() {
    if (fd == -1) return false;
    switch (api) {
      case V4L2_CAP_READWRITE:
        if (read_buffer != null) {
          free(read_buffer);
          read_buffer = null;
        }
        break;
      case V4L2_CAP_STREAMING:
        IntByReference type = new IntByReference();
        type.setValue(V4L2_BUF_TYPE_VIDEO_CAPTURE);
        xioctl(fd, VIDIOC_STREAMOFF, type.getPointer());
        //free buffers
        for(int i=0;i<2;i++) {
          c.munmap(mmapbuffers[i].start, mmapbuffers[i].length);
        }
        break;
    }
    c.close(fd);
    fd = -1;
    api = -1;
    return true;
  }

  public int[] getFrame() {
    switch (api) {
      case V4L2_CAP_READWRITE: return getFrame_read();
      case V4L2_CAP_STREAMING: return getFrame_stream();
    }
    return null;
  }

  private int[] getFrame_read() {
    if (c.read(fd, read_buffer, imagesize) <= 0) return null;
    readFrame(read_buffer, imagesize);
    return px;
  }

  private int[] getFrame_stream() {
    v4l2_buffer buffer;
    for(int i=0;i<2;i++) {
      buffer = new v4l2_buffer();
      buffer.type = mmapbuffers_type;
      buffer.memory = V4L2_MEMORY_MMAP;
      buffer.index = i;
      buffer.write();
      if (xioctl(fd, VIDIOC_QUERYBUF, buffer.getPointer()) == -1) {
        JFLog.log("LnxCamera:Failed to query buffer");
        return null;
      }
      buffer.read();
      if ((buffer.flags & V4L2_BUF_FLAG_DONE) == 0) continue;
      //dequeue
      if (xioctl(fd, VIDIOC_DQBUF, buffer.getPointer()) == -1) {
        JFLog.log("LnxCamera:Failed to dequeue buffer");
        return null;
      }
      buffer.read();
      //read memory
      readFrame(mmapbuffers[i].start, buffer.bytesused);
      //requeue
      if (xioctl(fd, VIDIOC_QBUF, buffer.getPointer()) == -1) {
        JFLog.log("LnxCamera:Failed to queue buffer");
        return null;
      }
      return px;
    }
    return null;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  private void readFrame(Pointer ptr, int length) {
    switch (pixfmt) {
      case V4L2_PIX_FMT_RGB32:
        readFrame_RGB32(ptr, length);
        break;
      case V4L2_PIX_FMT_YUYV:
        readFrame_YUYV(ptr, length);
        break;
      case V4L2_PIX_FMT_MJPG:
        readFrame_MJPG(ptr, length);
        break;
    }
  }

  private void readFrame_RGB32(Pointer ptr, int length) {
    if (bytesperline == width) {
      //copy all as is
      ptr.read(0, px, 0, width * height);
    } else {
      //copy each line
      int src_pos = 0;  //in bytes
      int dst_pos = 0;  //in pixels (32bit)
      for(int y=0;y<height;y++) {
        ptr.read(src_pos, px, dst_pos, width);
        src_pos += bytesperline;
        dst_pos += width;
      }
    }
  }

  private void readFrame_MJPG(Pointer ptr, int length) {
    byte buf[] = new byte[length];
    ptr.read(0, buf, 0, length);
    JFImage img = new JFImage();
    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
    if (!img.loadJPG(bais)) {
      JFLog.log("LnxCamera:Failed to load JPEG image");
      return;
    }
    px = img.getPixels();
  }

  //see http://www.fourcc.org/fccyvrgb.php for YUV <-> RGB conversions
  //Y=luminance U(Cb)=blue V(Cr)=red  (green is a mix of U/V)

  private int clamp(int v) {
    if (v < 0) return 0;
    if (v > 255) return 255;
    return v;
  }

  private void readFrame_YUYV(Pointer ptr, int length) {
    //YUYV format [Y0 U0 Y1 V0] = 2 pixels
    byte yuyv[] = new byte[length];
    ptr.read(0, yuyv, 0, length);
    int src = 0, dst = 0, r,g,b;
    float y0,u0,y1,v0;

    for(int y = 0;y < height;y++) {
      for(int x = 0;x < width;x+=2) {
        y0 = (((int)yuyv[src++]) & 0xff);
        u0 = (((int)yuyv[src++]) & 0xff);
        y1 = (((int)yuyv[src++]) & 0xff);
        v0 = (((int)yuyv[src++]) & 0xff);
        //pixel 1
        r = clamp((int)(1.164*(y0 - 16.0) + 1.596*(v0 - 128.0))) << 16;
        g = clamp((int)(1.164*(y0 - 16.0) - 0.813*(v0 - 128.0) - 0.391*(u0 - 128.0))) << 8;
        b = clamp((int)(1.164*(y0 - 16.0) + 2.018*(u0 - 128.0)));
        px[dst++] = (r + g + b) | 0xff000000;
        //pixel 2
        r = clamp((int)(1.164*(y1 - 16.0) + 1.596*(v0 - 128.0))) << 16;
        g = clamp((int)(1.164*(y1 - 16.0) - 0.813*(v0 - 128.0) - 0.391*(u0 - 128.0))) << 8;
        b = clamp((int)(1.164*(y1 - 16.0) + 2.018*(u0 - 128.0)));
        px[dst++] = (r + g + b) | 0xff000000;
      }
    }
  }
}
