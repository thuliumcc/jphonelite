package javaforce.jna;

/**
 * ALSA Sound for Linux using JNA
 *
 * @author pquiring
 *
 * Created : Sept 8, 2013
 */

import java.util.*;

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import javaforce.*;
import javaforce.media.Sound;

public class LnxSound {
  private static Pointer malloc(int size) {
    return new Pointer(Native.malloc(size));
  }

  private static void free(Pointer ptr) {
    Native.free(Pointer.nativeValue(ptr));
  }

  //all functions return < 0 on error unless noted
  public static interface ASound extends Library {
    public int snd_pcm_open(PointerByReference handle_ref, String name, int stream, int mode);
    public int snd_pcm_hw_params_malloc(PointerByReference params_ref);
    public int snd_pcm_hw_params_any(Pointer handle, Pointer params);
    public int snd_pcm_hw_params_set_access(Pointer handle, Pointer params, int access);
    public int snd_pcm_hw_params_set_format(Pointer handle, Pointer params, int format);
    public int snd_pcm_hw_params_set_rate(Pointer handle, Pointer params, int rate, int dir);  //freq Hz
    public int snd_pcm_hw_params_set_rate_near(Pointer handle, Pointer params, IntByReference rate_ref, IntByReference dir_ref);  //freq Hz
    public int snd_pcm_hw_params_set_channels(Pointer handle, Pointer params, int chs);
    public int snd_pcm_hw_params_set_buffer_size(Pointer handle, Pointer params, int frames);
    public int snd_pcm_hw_params(Pointer handle, Pointer params);
    public int snd_pcm_hw_params_free(Pointer params);
    public int snd_pcm_prepare(Pointer handle);
    public int snd_pcm_writei(Pointer handle, Pointer buffer, int frames);  //returns write count
    public int snd_pcm_readi(Pointer handle, Pointer buffer, int frames);  //returns read count
    public int snd_pcm_close(Pointer handle);
    public int snd_pcm_recover(Pointer handle, int err, int silent);
    public int snd_pcm_start(Pointer handle);
    //enumerating
    public int snd_card_next(IntByReference ref);
    public int snd_get_card_name(int card, PointerByReference str_ref);
    public int snd_device_name_hint(int card, String iface, PointerByReference ptr_list);  //iface = "pcm"
    public int snd_device_name_free_hint(Pointer list);
    public Pointer snd_device_name_get_hint(Pointer hint, String id);  //id = "NAME"
  }

  private static ASound asound;
  private static final int SND_PCM_STREAM_PLAYBACK = 0;
  private static final int SND_PCM_STREAM_CAPTURE = 1;
  private static final int SND_PCM_ACCESS_RW_INTERLEAVED = 3;
  private static final int SND_8 = 0;
  private static final int SND_16 = 2;  //BE=3
  private static final int SND_24 = 6;  //BE=7
  private static final int SND_32 = 10;  //BE=11

  public static boolean init() {
    try {
      if (asound == null) {
        asound = (ASound)Native.loadLibrary("asound", ASound.class);
      }
    } catch (Throwable t) {
      JFLog.log(t);
      return false;
    }
    return true;
  }

  private static String[] listDevices() {
    ArrayList<String> list = new ArrayList<String>();
    list.add("<default>");
    IntByReference card_ref = new IntByReference();
    card_ref.setValue(-1);
    do {
      asound.snd_card_next(card_ref);
      if (card_ref.getValue() == -1) break;
      PointerByReference hints_ref = new PointerByReference();
      asound.snd_device_name_hint(card_ref.getValue(), "pcm", hints_ref);
      Pointer hints = hints_ref.getValue();
      if (hints == null) continue;
      int idx = 0;
      do {
        Pointer hint = hints.getPointer(idx * Pointer.SIZE);
        if (hint == null) break;
        Pointer name = asound.snd_device_name_get_hint(hint, "NAME");
        if (name == null) break;
        String namestr = name.getString(0);
        if (!namestr.equals("null")) list.add(namestr);
        free(name);
        if (namestr.equals("null")) break;
        idx++;
      } while (true);
      asound.snd_device_name_free_hint(hints);
    } while (true);
    return list.toArray(new String[0]);
  }

  public static class Output implements Sound.Output {
    private Pointer handle;
    private Pointer buffer;
    private int bytes, chs;

    public String[] listDevices() {
      return LnxSound.listDevices();
    }
    public boolean start(int chs, int freq, int bits, int bufsiz, String device) {
      if (device == null || device.equals("<default>")) device = "plughw:0,0";
      this.chs = chs;
      PointerByReference handle_ref = new PointerByReference();
      if (asound.snd_pcm_open(handle_ref, device, SND_PCM_STREAM_PLAYBACK, 0) < 0) {
        return false;
      }
      handle = handle_ref.getValue();
      PointerByReference params_ref = new PointerByReference();
      if (asound.snd_pcm_hw_params_malloc(params_ref) < 0) {
        return false;
      }
      Pointer params = params_ref.getValue();
      if (asound.snd_pcm_hw_params_any(handle, params) < 0) {
        return false;
      }
      if (asound.snd_pcm_hw_params_set_access(handle, params, SND_PCM_ACCESS_RW_INTERLEAVED) < 0) {
        return false;
      }
      int fmt;
      switch (bits) {
        case 8: fmt = SND_8; break;
        case 16: fmt = SND_16; break;
        case 24: fmt = SND_24; break;
        case 32: fmt = SND_32; break;
        default: return false;
      }
      bytes = bits / 8;
      if (asound.snd_pcm_hw_params_set_format(handle, params, fmt) < 0) {
        return false;
      }
      if (asound.snd_pcm_hw_params_set_rate(handle, params, freq, 0) < 0) {
        return false;
      }
      if (asound.snd_pcm_hw_params_set_channels(handle, params, chs) < 0) {
        return false;
      }
/*      if (asound.snd_pcm_hw_params_set_buffer_size(handle, params, bufsiz / bytes / chs) < 0) {
        JFLog.log("LnxSound:Warning:Failed to lower latency");
      }*/
      if (asound.snd_pcm_hw_params(handle, params) < 0) {
        return false;
      }
      if (asound.snd_pcm_hw_params_free(params) < 0) {
        return false;
      }
      if (asound.snd_pcm_prepare(handle) < 0) {
        return false;
      }
      buffer = malloc(bufsiz);
      return true;
    }

    public boolean write(byte[] buf) {
      if (buffer == null) return false;
      buffer.write(0, buf, 0, buf.length);
      int frames = buf.length / bytes / chs;
      int ret = asound.snd_pcm_writei(handle, buffer, frames);
      if (ret < 0) {
        asound.snd_pcm_recover(handle, ret, 1);
        return false;
      }
      return ret == frames;
    }

    public boolean write(short[] buf) {
      if (buffer == null) return false;
      buffer.write(0, buf, 0, buf.length);
      int frames = (buf.length * 2) / bytes / chs;
      int ret = asound.snd_pcm_writei(handle, buffer, frames);
      if (ret < 0) {
        asound.snd_pcm_recover(handle, ret, 1);
        return false;
      }
      return ret == frames;
    }

    public boolean stop() {
      if (buffer != null) {
        free(buffer);
        buffer = null;
      }
      if (handle != null) {
        asound.snd_pcm_close(handle);
        handle = null;
      }
      return true;
    }
  }

  public static class Input implements Sound.Input {
    private Pointer handle;
    private Pointer buffer;
    private int bytes, chs;

    public String[] listDevices() {
      return LnxSound.listDevices();
    }
    public boolean start(int chs, int freq, int bits, int bufsiz, String device) {
      if (device == null || device.equals("<default>")) device = "plughw:0,0";
      this.chs = chs;
      PointerByReference handle_ref = new PointerByReference();
      if (asound.snd_pcm_open(handle_ref, device, SND_PCM_STREAM_CAPTURE, 0) < 0) {
        return false;
      }
      handle = handle_ref.getValue();
      PointerByReference params_ref = new PointerByReference();
      if (asound.snd_pcm_hw_params_malloc(params_ref) < 0) {
        return false;
      }
      Pointer params = params_ref.getValue();
      if (asound.snd_pcm_hw_params_any(handle, params) < 0) {
        return false;
      }
      if (asound.snd_pcm_hw_params_set_access(handle, params, SND_PCM_ACCESS_RW_INTERLEAVED) < 0) {
        return false;
      }
      int fmt;
      switch (bits) {
        case 8: fmt = SND_8; break;
        case 16: fmt = SND_16; break;
        case 24: fmt = SND_24; break;
        case 32: fmt = SND_32; break;
        default: return false;
      }
      bytes = bits / 8;
      if (asound.snd_pcm_hw_params_set_format(handle, params, fmt) < 0) {
        return false;
      }
      if (asound.snd_pcm_hw_params_set_rate(handle, params, freq, 0) < 0) {
        return false;
      }
      if (asound.snd_pcm_hw_params_set_channels(handle, params, chs) < 0) {
        return false;
      }
/*      if (asound.snd_pcm_hw_params_set_buffer_size(handle, params, bufsiz / bytes / chs) < 0) {
        JFLog.log("LnxSound:Warning:Failed to lower latency");
      }*/
      if (asound.snd_pcm_hw_params(handle, params) < 0) {
        return false;
      }
      if (asound.snd_pcm_hw_params_free(params) < 0) {
        return false;
      }
      if (asound.snd_pcm_prepare(handle) < 0) {
        return false;
      }
      if (asound.snd_pcm_start(handle) > 0) {
        return false;
      }
      buffer = malloc(bufsiz);
      return true;
    }

    public boolean read(byte[] buf) {
      if (buffer == null) return false;
      int frames = buf.length / bytes / chs;
      int ret = asound.snd_pcm_readi(handle, buffer, frames);
      if (ret != frames) {
        System.out.println("LnxSound:read="+ ret);
        asound.snd_pcm_recover(handle, ret, 1);
        return false;
      }
      buffer.read(0, buf, 0, buf.length);
      return true;
    }

    public boolean read(short[] buf) {
      if (buffer == null) return false;
      int frames = buf.length * 2 / bytes / chs;
      int ret = asound.snd_pcm_readi(handle, buffer, frames);
      if (ret != frames) {
        System.out.println("LnxSound:read="+ ret);
        asound.snd_pcm_recover(handle, ret, 1);
        return false;
      }
      buffer.read(0, buf, 0, buf.length);
      return true;
    }

    public boolean stop() {
      if (buffer != null) {
        free(buffer);
        buffer = null;
      }
      if (handle != null) {
        asound.snd_pcm_close(handle);
        handle = null;
      }
      return true;
    }
  }
}
