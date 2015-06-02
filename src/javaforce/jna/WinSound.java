package javaforce.jna;

/**
 * Windows Sound API
 *
 * @author pquiring
 *
 * Created : Aug 16, 2013
 */

import java.lang.reflect.*;
import java.util.*;

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.*;

import javaforce.*;
import javaforce.media.*;

public class WinSound {
  private static Pointer malloc(int size) {
    return new Pointer(Native.malloc(size));
  }

  private static void free(Pointer ptr) {
    Native.free(Pointer.nativeValue(ptr));
  }
  public static class WaveFormatEx extends Structure {
    public short wFormatTag;
    public short nChannels;
    public int nSamplesPerSec;
    public int nAvgBytesPerSec;
    public short nBlockAlign;
    public short wBitsPerSample;
    public short cbSize;
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
  }
  public static class WaveHdr extends Structure {
    public Pointer lpData;
    public int dwBufferLength;
    public int dwBytesRecorded;
    public Pointer dwUser;
    public int dwFlags;
    public int dwLoops;
    public Pointer lpNext;
    public Pointer reserved;
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
  }
  public static class WaveOutCaps extends Structure {
    public short Mid, Pid;
    public int Version;
    public byte name[] = new byte[32];
    public int formats;
    public short channels, reserved;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
  }
  public static class WaveInCaps extends Structure {
    public short Mid, Pid;
    public int Version;
    public byte name[] = new byte[32];
    public int formats;
    public short channels, reserved;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
  }
  public static interface WinMM extends StdCallLibrary {
    public int waveOutOpen(PointerByReference handle, Pointer deviceId, WaveFormatEx waveFormat
      , Pointer callback, Pointer callbackInst, int flags);
    public int waveOutPrepareHeader(Pointer handle, WaveHdr hdr, int hdrSize);
    public int waveOutWrite(Pointer handle, WaveHdr hdr, int hdrSize);
    public int waveOutReset(Pointer handle);
    public int waveOutClose(Pointer handle);

    public int waveInOpen(PointerByReference handle, Pointer deviceId, WaveFormatEx waveFormat
      , Pointer callback, Pointer callbackInst, int flags);
    public int waveInPrepareHeader(Pointer handle, WaveHdr hdr, int hdrSize);
    public int waveInAddBuffer(Pointer handle, WaveHdr hdr, int hdrSize);
    public int waveInStart(Pointer handle);
    public int waveInStop(Pointer handle);
    public int waveInClose(Pointer handle);

    //enumerating
    public int waveOutGetNumDevs();
    public int waveOutGetDevCapsA(Pointer handle, WaveOutCaps caps, int sizeofCaps);

    public int waveInGetNumDevs();
    public int waveInGetDevCapsA(Pointer handle, WaveInCaps caps, int sizeofCaps);
  }
  public static interface KERNEL extends StdCallLibrary {
    public int GetLastError();
  }

  private static List makeFieldList(Class cls) {
    //This "assumes" compiler places fields in order as defined (some don't)
    ArrayList<String> list = new ArrayList<String>();
    Field fields[] = cls.getFields();
    for(int a=0;a<fields.length;a++) {
      String name = fields[a].getName();
      if (name.startsWith("ALIGN_")) continue;  //field of Structure
      list.add(name);
    }
    return list;
  }

  private static WinMM winmm;
  private static KERNEL kernel;
  private static final int NUM_BUFFERS = 16;

  public static boolean init() {
    if (!JF.isWindows()) return false;
    try {
      if (winmm == null) {
        winmm = (WinMM) Native.loadLibrary("winmm", WinMM.class);
      }
      if (kernel == null) {
        kernel = (KERNEL) Native.loadLibrary("kernel32", KERNEL.class);
      }
    } catch (Throwable t) {
      JFLog.log(t);
      return false;
    }
    return true;
  }

  private static final int WAVE_ALLOWSYNC = 0x0002;
  private static final long WAVE_MAPPER = -1;
  private static final int WAVE_FORMAT_PCM = 1;
  private static final int WHDR_DONE = 0x0001;
  private static final int WHDR_INQUEUE = 0x0010;

  private static int strlen(byte str[]) {
    int max = str.length;
    for(int a=0;a<max;a++) {
      if (str[a] == 0) return a;
    }
    return max;
  }

  public static class Output implements Sound.Output {
    private Pointer handle = null;
    private WaveHdr bufs[] = new WaveHdr[NUM_BUFFERS];
    private String devices[] = new String[0];

    public String[] listDevices() {
      ArrayList<String> list = new ArrayList<String>();
      int cnt = winmm.waveOutGetNumDevs();
      WaveOutCaps caps = new WaveOutCaps();
      for(int a=0;a<cnt;a++) {
        winmm.waveOutGetDevCapsA(new Pointer(a), caps, caps.size());
        String name = new String(caps.name, 0, strlen(caps.name));
        list.add(name);
      }
      devices = list.toArray(new String[0]);;
      return devices;
    }
    public boolean start(int chs, int freq, int bits, int bufsiz, String device) {
      int bytes = -1;
      switch (bits) {
        case 8: bytes = 1; break;
        case 16: bytes = 2; break;
      }
      WaveFormatEx wfex = new WaveFormatEx();
      wfex.wFormatTag = WAVE_FORMAT_PCM;
      wfex.nChannels = (short)chs;
      wfex.nSamplesPerSec = freq;
      wfex.nAvgBytesPerSec = chs * freq * bytes;
      wfex.nBlockAlign = (short)(chs * bytes);
      wfex.wBitsPerSample = (short)bits;
      wfex.cbSize = 0; //no extra info

      long deviceid = WAVE_MAPPER;
      for(int a=0;a<devices.length;a++) {
        if (device.equals(devices[a])) {
          deviceid = a;
          break;
        }
      }

      PointerByReference handle_ref = new PointerByReference();

      int ret = winmm.waveOutOpen(handle_ref, new Pointer(deviceid), wfex, null, null, WAVE_ALLOWSYNC);
      if (ret != 0) {
        JFLog.log("waveOutOpen() Failed:ret=" + ret);
        return false;
      }

      handle = handle_ref.getValue();

      //prep buffers
      for(int a=0;a<NUM_BUFFERS;a++) {
        bufs[a] = new WaveHdr();
        bufs[a].lpData = malloc(bufsiz);
        bufs[a].dwBufferLength = bufsiz;
        ret = winmm.waveOutPrepareHeader(handle, bufs[a], bufs[a].size());
        if (ret != 0) {
          JFLog.log("waveOutPrepareHeader() Failed");
          stop();
          return false;
        }
      }

      return true;
    }

    public boolean write(byte buf[]) {
      if (handle == null) return false;
      //search for available buffer
      for(int a=0;a<NUM_BUFFERS;a++) {
        bufs[a].read();
        if ((bufs[a].dwFlags & WHDR_INQUEUE) == 0) {
          bufs[a].lpData.write(0, buf, 0, buf.length);
          winmm.waveOutWrite(handle, bufs[a], bufs[a].size());
          return true;
        }
      }
      return false;
    }

    public boolean write(short buf[]) {
      //search for available buffer
      for(int a=0;a<NUM_BUFFERS;a++) {
        bufs[a].read();
        if ((bufs[a].dwFlags & WHDR_INQUEUE) == 0) {
          bufs[a].lpData.write(0, buf, 0, buf.length);
          winmm.waveOutWrite(handle, bufs[a], bufs[a].size());
          return true;
        }
      }
      return false;
    }

    public boolean stop() {
      if (handle == null) return true;
      winmm.waveOutReset(handle);
      winmm.waveOutClose(handle);
      handle = null;
      for(int a=0;a<NUM_BUFFERS;a++) {
        if (bufs[a] == null) continue;
        if (bufs[a].lpData != null) {
          free(bufs[a].lpData);
        }
        bufs[a].lpData = null;
        bufs[a] = null;
      }
      return true;
    }

    /** Returns a count of buffers that are full */
    public int buffersFull() {
      int cnt = 0;
      for(int a=0;a<NUM_BUFFERS;a++) {
        bufs[a].read();
        if ((bufs[a].dwFlags & WHDR_INQUEUE) != 0) {
          cnt++;
        }
      }
      return cnt;
    }
  }

  public static class Input implements Sound.Input {
    private Pointer handle;
    private WaveHdr bufs[] = new WaveHdr[NUM_BUFFERS];
    private String devices[] = new String[0];

    public String[] listDevices() {
      ArrayList<String> list = new ArrayList<String>();
      int cnt = winmm.waveInGetNumDevs();
      WaveInCaps caps = new WaveInCaps();
      for(int a=0;a<cnt;a++) {
        winmm.waveInGetDevCapsA(new Pointer(a), caps, caps.size());
        String name = new String(caps.name, 0, strlen(caps.name));
        list.add(name);
      }
      devices = list.toArray(new String[0]);;
      return devices;
    }
    public boolean start(int chs, int freq, int bits, int bufsiz, String device) {
      int bytes = -1;
      switch (bits) {
        case 8: bytes = 1; break;
        case 16: bytes = 2; break;
      }
      WaveFormatEx wfex = new WaveFormatEx();
      wfex.wFormatTag = WAVE_FORMAT_PCM;
      wfex.nChannels = (short)chs;
      wfex.nSamplesPerSec = freq;
      wfex.nAvgBytesPerSec = freq * bytes;
      wfex.nBlockAlign = (short)bytes;
      wfex.wBitsPerSample = (short)bits;
      wfex.cbSize = 0; //no extra info

      long deviceid = WAVE_MAPPER;
      for(int a=0;a<devices.length;a++) {
        if (device.equals(devices[a])) {
          deviceid = a;
          break;
        }
      }

      PointerByReference handle_ref = new PointerByReference();

      int ret = winmm.waveInOpen(handle_ref, new Pointer(deviceid), wfex, null, null, 0);
      if (ret != 0) {
        JFLog.log("waveInOpen() Failed:error=" + kernel.GetLastError());
        return false;
      }

      handle = handle_ref.getValue();

      //prep buffers
      for(int a=0;a<NUM_BUFFERS;a++) {
        bufs[a] = new WaveHdr();
        bufs[a].lpData = malloc(bufsiz);
        bufs[a].dwBufferLength = bufsiz;
        ret = winmm.waveInPrepareHeader(handle, bufs[a], bufs[a].size());
        if (ret != 0) {
          JFLog.log("waveInPrepareHeader() Failed");
          stop();
          return false;
        }
        ret = winmm.waveInAddBuffer(handle, bufs[a], bufs[a].size());
        if (ret != 0) {
          JFLog.log("waveInAddBuffer() Failed");
          stop();
          return false;
        }
      }

      ret = winmm.waveInStart(handle);
      if (ret != 0) {
        JFLog.log("waveInStart() Failed");
        stop();
        return false;
      }

      return true;
    }

    private int pos = 0;  //last buffer used

    public boolean read(byte buf[]) {
      //search for available buffer
      for(int a=0;a<NUM_BUFFERS;a++) {
        bufs[pos].read();
        if ((bufs[pos].dwFlags & WHDR_DONE) != 0) {
          bufs[pos].lpData.read(0, buf, 0, buf.length);
          winmm.waveInAddBuffer(handle, bufs[pos], bufs[pos].size());
          pos++;
          if (pos == NUM_BUFFERS) pos = 0;
          return true;
        }
        pos++;
        if (pos == NUM_BUFFERS) pos = 0;
      }
      return false;
    }

    public boolean read(short buf[]) {
      //search for available buffer
      for(int a=0;a<NUM_BUFFERS;a++) {
        bufs[pos].read();
        if ((bufs[pos].dwFlags & WHDR_DONE) != 0) {
          bufs[pos].lpData.read(0, buf, 0, buf.length);
          winmm.waveInAddBuffer(handle, bufs[pos], bufs[pos].size());
          pos++;
          if (pos == NUM_BUFFERS) pos = 0;
          return true;
        }
        pos++;
        if (pos == NUM_BUFFERS) pos = 0;
      }
      return false;
    }

    public boolean stop() {
      if (handle == null) return true;
      winmm.waveInStop(handle);
      winmm.waveInClose(handle);
      handle = null;
      for(int a=0;a<NUM_BUFFERS;a++) {
        if (bufs[a] == null) continue;
        if (bufs[a].lpData != null) {
          free(bufs[a].lpData);
        }
        bufs[a].lpData = null;
        bufs[a] = null;
      }
      return true;
    }
  }
}
