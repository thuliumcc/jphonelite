package javaforce.media;

/**
 * Common Sound API (Java, Windows API, Linux API)
 *
 * @author pquiring
 *
 * Created : Aug 16, 2013
 */

import java.util.ArrayList;
import javax.sound.sampled.*;

import javaforce.*;
import javaforce.jna.*;

public class Sound {
  public static interface Output {
    public String[] listDevices();
    public boolean start(int chs, int freq, int bits, int bufsiz, String device);
    public boolean write(byte buf[]);
    public boolean write(short buf[]);
    public boolean stop();
  }
  public static Output getOutput(boolean useNative) {
    if (useNative) {
      if (JF.isWindows()) {
        if (WinSound.init()) {
          return new WinSound.Output();
        }
      } else {
        if (LnxSound.init()) {
          return new LnxSound.Output();
        }
      }
    }
    return new JavaOutput();
  }

  private static class JavaOutput implements Output {
    private SourceDataLine sdl;
    private AudioFormat af;
    private byte buf8[];
    public String[] listDevices() {
      ArrayList<String> mixers = new ArrayList<String>();
      Mixer.Info mi[] = AudioSystem.getMixerInfo();
      mixers.add("<default>");
      for(int a=0;a<mi.length;a++) {
        String name = mi[a].getName();
        Mixer m = AudioSystem.getMixer(mi[a]);
        if (m.getSourceLineInfo().length == 0) continue; //no source lines
        mixers.add(name);
      }
      return mixers.toArray(new String[0]);
    }
    public boolean start(int chs, int freq, int bits, int bufsiz, String device) {
      buf8 = new byte[bufsiz];
      if (device == null) device = "<default>";
      af = new AudioFormat((float)freq, bits, chs, true, true);
      Mixer.Info mi[] = AudioSystem.getMixerInfo();
      int idx = -1;
      for(int a=0;a<mi.length;a++) {
        if (mi[a].getName().equalsIgnoreCase(device)) {
          idx = a;
          break;
        }
      }
      try {
        if (idx == -1) {
          sdl = AudioSystem.getSourceDataLine(af);
        } else {
          sdl = AudioSystem.getSourceDataLine(af, mi[idx]);
        }
      } catch (Exception e) {
        JFLog.log(e);
        return false;
      }
      try {
        sdl.open(af);
      } catch (Exception e) {
        JFLog.log(e);
        return false;
      }
      sdl.start();
      return true;
    }
    public boolean write(byte buf[]) {
      sdl.write(buf, 0, buf.length);
      return true;
    }
    //big endian (java sound)
    private byte[] short2byte(short in[], byte out[]) {
      for (int a = 0; a < in.length; a++) {
        out[a * 2] = (byte) (in[a] >>> 8);
        out[a * 2 + 1] = (byte) (in[a] & 0xff);
      }
      return out;
    }
    public boolean write(short buf16[]) {
      sdl.write(short2byte(buf16, buf8), 0, buf16.length * 2);
      return true;
    }
    public boolean stop() {
      if (sdl == null) return false;
      sdl.stop();
      sdl.close();
      sdl = null;
      return true;
    }
  }

  public static interface Input {
    public String[] listDevices();
    public boolean start(int chs, int freq, int bits, int bufsiz, String device);
    public boolean read(byte buf[]);
    public boolean read(short buf[]);
    public boolean stop();
  }

  public static Input getInput(boolean useNative) {
    if (useNative) {
      if (JF.isWindows()) {
        if (WinSound.init()) {
          return new WinSound.Input();
        }
      } else {
        if (LnxSound.init()) {
          return new LnxSound.Input();
        }
      }
    }
    return new JavaInput();
  }

  private static class JavaInput implements Input {
    private TargetDataLine tdl;
    private AudioFormat af;
    private byte buf8[];
    public String[] listDevices() {
      ArrayList<String> mixers = new ArrayList<String>();
      Mixer.Info mi[] = AudioSystem.getMixerInfo();
      mixers.add("<default>");
      for(int a=0;a<mi.length;a++) {
        String name = mi[a].getName();
        Mixer m = AudioSystem.getMixer(mi[a]);
        if (m.getTargetLineInfo().length == 0) continue;  //no target lines
        mixers.add(name);
      }
      return mixers.toArray(new String[0]);
    }
    public boolean start(int chs, int freq, int bits, int bufsiz, String device) {
      buf8 = new byte[bufsiz];
      if (device == null) device = "<default>";
      af = new AudioFormat((float)freq, bits, chs, true, true);
      Mixer.Info mi[] = AudioSystem.getMixerInfo();
      int idx = -1;
      for(int a=0;a<mi.length;a++) {
        if (mi[a].getName().equalsIgnoreCase(device)) {
          idx = a;
          break;
        }
      }
      try {
        if (idx == -1) {
          tdl = AudioSystem.getTargetDataLine(af);
        } else {
          tdl = AudioSystem.getTargetDataLine(af, mi[idx]);
        }
      } catch (Exception e) {
        JFLog.log(e);
        return false;
      }
      try {
        tdl.open(af);
      } catch (Exception e) {
        JFLog.log(e);
        return false;
      }
      tdl.start();
      return true;
    }
    public boolean read(byte buf[]) {
      if (tdl.available() < buf.length) return false;  //do not block (causes audio glitches)
      int ret = tdl.read(buf, 0, buf.length);
      if (ret != buf.length) return false;
      return true;
    }
    private void byte2short(byte buf8[], short buf16[]) {
      for (int a = 0; a < 160; a++) {
        buf16[a] = (short) ((((short) (buf8[a * 2])) << 8) + (((short) (buf8[a * 2 + 1])) & 0xff));
      }
    }
    public boolean read(short buf16[]) {
      if (!read(buf8)) return false;
      byte2short(buf8, buf16);
      return true;
    }
    public boolean stop() {
      if (tdl == null) return false;
      tdl.stop();
      tdl.close();
      tdl = null;
      return true;
    }
  }
}
