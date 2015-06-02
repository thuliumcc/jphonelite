import java.util.*;

import javaforce.*;
import javaforce.media.*;
import javaforce.voip.*;

/** Handles all aspects of sound processing (recording, playback, ringing sounds, conference mixing, etc.) */

public class Sound {
  //sound data
  private short silence[] = new short[160];
  private short mixed[] = new short[160];
  private short recording[] = new short[160];
  private short indata[] = new short[160];
  private short outdata[] = new short[160];
  private short dataDown32k[] = new short[640];
  private short dataDown44_1k[] = new short[882];
  private short ringing[] = new short[160];
  private short callWaiting[] = new short[160];
  private javaforce.media.Sound.Output output;
  private javaforce.media.Sound.Input input;
  private Timer timer;
  private Player player;
  private PhoneLine lines[];
  private int line = -1;
  private boolean inRinging = false, outRinging = false;
  private MeterController mc;
  private int volPlay = 100, volRec = 100;
  private boolean mute = false;
  private DTMF dtmf = new DTMF();
  private boolean playing = false;
  private javaforce.voip.Wav inWav, outWav;
  private int speakerDelay = 0;
  private int sampleRate, sampleRate50, sampleRate50x2;

  /** Init sound system.  Sound needs access to the lines and the MeterController to send audio levels back to the panel. */

  public boolean init(PhoneLine lines[], MeterController mc) {
    this.lines = lines;
    this.mc = mc;
    sampleRate = Settings.current.sampleRate;
    sampleRate50 = sampleRate / 50;
    sampleRate50x2 = sampleRate50 * 2;
    //setup inbound ring tone
    if (Settings.current.inRingtone.startsWith("*")) {
      if (Settings.current.inRingtone.equals("*RING")) {
        inWav = new javaforce.voip.Wav();
        inWav.load(getClass().getResourceAsStream("ringing.wav"));
      } else if (Settings.current.inRingtone.equals("*NA")) {
        initInRinging(440, 480, 2000, 4000, 2000, 4000);  //north america
      } else if (Settings.current.inRingtone.equals("*UK")) {
        initInRinging(400, 450, 400, 200, 400, 2000);  //UK
      } else {
        inWav = new javaforce.voip.Wav();
        inWav.load(getClass().getResourceAsStream("ringing.wav"));
      }
    } else {
      inWav = new javaforce.voip.Wav();
      if (!inWav.load(Settings.current.inRingtone)) {
        JFLog.log("Failed to load : " + Settings.current.inRingtone);
        inWav = new javaforce.voip.Wav();
        inWav.load(getClass().getResourceAsStream("ringing.wav"));
      }
    }
    //setup outbound ringback tone
    if (Settings.current.outRingtone.startsWith("*")) {
      if (Settings.current.outRingtone.equals("*RING")) {
        outWav = new javaforce.voip.Wav();
        outWav.load(getClass().getResourceAsStream("ringing.wav"));
      } else if (Settings.current.outRingtone.equals("*NA")) {
        initOutRinging(440, 480, 2000, 4000, 2000, 4000);  //north america
      } else if (Settings.current.outRingtone.equals("*UK")) {
        initOutRinging(400, 450, 400, 200, 400, 2000);  //UK
      } else {
        initOutRinging(440, 480, 2000, 4000, 2000, 4000);  //north america
      }
    } else {
      outWav = new javaforce.voip.Wav();
      if (!outWav.load(Settings.current.outRingtone)) {
        JFLog.log("Failed to load : " + Settings.current.outRingtone);
        initOutRinging(440, 480, 2000, 4000, 2000, 4000);  //north america
      }
    }

    output = javaforce.media.Sound.getOutput(Settings.current.nativeSound);
    input = javaforce.media.Sound.getInput(Settings.current.nativeSound);
    System.out.println("output=" + output + ",input=" + input);
    output.listDevices();
    input.listDevices();
    if (!input.start(1, 8000, 16, sampleRate50x2, Settings.current.audioInput)) {
      JFLog.log("Input.start() failed");
      return false;
    }

    if (Settings.current.keepAudioOpen) {
      if (!output.start(1, 8000, 16, sampleRate50x2, Settings.current.audioOutput)) {
        JFLog.log("Output.start() failed");
        return false;
      }
      for(int a=0;a<2;a++) write(silence);  //prime output
    }
    player = new Player();
    player.start();
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
        process();
      }
    }, 0, 20);
    return true;
  }

  int snd_id_play = -1, snd_id_record = -1;

  /** Frees resources. */

  public void uninit() {
    if (timer != null) {
      timer.cancel();
      timer = null;
      JF.sleep(25);  //wait for any pending timer events
    }
    if (player != null) {
      player.cancel();
      player = null;
    }
    if (record != null) {
      record.close();
      record = null;
    }
    if (Settings.current.keepAudioOpen) {
      output.stop();
    } else {
      if (playing) {
        output.stop();
        playing = false;
      }
    }
    mc.setMeterPlay(0);
    input.stop();
    inWav = null;
    outWav = null;
  }

  /** Returns if software volume control on recording. */

  public boolean isSWVolRec() {
    return true;
  }

  /** Returns if software volume control on playing. */

  public boolean isSWVolPlay() {
    return true;
  }

  /** Changes which line user wants to listen to. */

  public void selectLine(int line) {
    this.line = line;
  }

  /** Changes software/hardware playback volume level. */

  public void setVolPlay(int lvl) {
    volPlay = lvl;
    Settings.current.volPlaySW = volPlay;
//    Settings.saveSettings();
  }

  /** Changes software/hardware recording volume level. */

  public void setVolRec(int lvl) {
    volRec = lvl;
    Settings.current.volRecSW = volRec;
//    Settings.saveSettings();
  }

  /** Sets mute state. */

  public void setMute(boolean state) {
    mute = state;
  }

  /** Scales samples to a software volume control. */

  private void scaleBufferVolume(short buf[], int start, int len, int scale) {
    float fscale;
    if (scale == 0) {
      for (int a = 0; a < 160; a++) {
        buf[a] = 0;
      }
    } else {
      if (scale <= 75) {
        fscale = 1.0f - ((75-scale) * 0.014f);
        for (int a = 0; a < 160; a++) {
          buf[a] = (short) (buf[a] * fscale);
        }
      } else {
        fscale = 1.0f + ((scale-75) * 0.04f);
        float value;
        for (int a = 0; a < 160; a++) {
          value = buf[a] * fscale;
          if (value < Short.MIN_VALUE) buf[a] = Short.MIN_VALUE;
          else if (value > Short.MAX_VALUE) buf[a] = Short.MAX_VALUE;
          else buf[a] = (short)value;
        }
      }
    }
  }

  private short lastSampleUp = 0;

  /** Scales a buffer from 8000hz to 44100hz (linear interpolated) */

  private short[] scaleBufferFreqUp44_1kLinear(short inBuf[], short outBuf[]) {
    //8000 = 160 samples
    //44100 = 882 samples
    //ratio = 5.5125
    int outPos = 0, inPos = 0;
    float v1 = lastSampleUp, v2 = 0;
    int i;
    int im = 0;
    float d;
    for(int a=0;a<160;a++) {
      v2 = inBuf[inPos++];
      i = 5;
      im += 5125;
      if (im >= 10000) {
        im -= 10000;
        i++;
      }
      d = (v2 - v1) / i;
      for(int b=0;b<i;b++) {
        outBuf[outPos++] = (short)v1;
        v1 += d;
      }
      v1 = v2;
    }
    lastSampleUp = (short)v2;
    return outBuf;
  }

  /** Scales a buffer from 8000hz to 32000hz (linear interpolated) */

  private short[] scaleBufferFreqUp32kLinear(short inBuf[], short outBuf[]) {
    //8000 = 160 samples
    //32000 = 640 samples
    //ratio = 4.0
    int outPos = 0, inPos = 0;
    float v1 = lastSampleUp, v2 = 0;
    int i;
    int im = 0;
    float d;
    for(int a=0;a<160;a++) {
      v2 = inBuf[inPos++];
      d = (v2 - v1) / 4;
      for(int b=0;b<4;b++) {
        outBuf[outPos++] = (short)v1;
        v1 += d;
      }
      v1 = v2;
    }
    lastSampleUp = (short)v2;
    return outBuf;
  }

  /*
Filter Cap.
Causes the waveform to slope gradually just like a capacitor would in a real audio circuit.
(I knew my electronics degree was good for something)
  */

  /** Scales a buffer from 8000hz to 44100hz (filter cap interpolated) */

  private short[] scaleBufferFreqUp44_1kFilterCap(short inBuf[], short outBuf[]) {
    //8000 = 160 samples
    //44100 = 882 samples
    //ratio = 5.5125
    int outPos = 0, inPos = 0;
    float v1 = lastSampleUp, v2 = 0;
    int i;
    int im = 0;
    float d, c, x, y;  //delta per step, total change at this step, scale factor, scale factor per step
    for(int a=0;a<160;a++) {
      v2 = inBuf[inPos++];
      i = 5;
      im += 5125;
      if (im >= 10000) {
        im -= 10000;
        i++;
      }
      d = (v2 - v1) / i;
      c = d;
      y = 1.0f / i;
      x = y;
      for(int b=0;b<i;b++) {
        outBuf[outPos++] = (short)(v1 + (c * x));
        c += d;
        x += y;
      }
      v1 = v2;
    }
    lastSampleUp = (short)v2;
    return outBuf;
  }

  /** Scales a buffer from 8000hz to 32000hz (filter cap interpolated) */

  private short[] scaleBufferFreqUp32kFilterCap(short inBuf[], short outBuf[]) {
    //8000 = 160 samples
    //32000 = 640 samples
    //ratio = 4.0
    int outPos = 0, inPos = 0;
    float v1 = lastSampleUp, v2 = 0;
    int i;
    int im = 0;
    float d, c, x;  //delta per step, total change at this step, scale factor
    for(int a=0;a<160;a++) {
      v2 = inBuf[inPos++];
      d = (v2 - v1) / 4.0f;
      c = d;
      x = 0.25f;
      for(int b=0;b<4;b++) {
        outBuf[outPos++] = (short)(v1 + (c * x));
        c += d;
        x += 0.25f;
      }
      v1 = v2;
    }
    lastSampleUp = (short)v2;
    return outBuf;
  }

  /** Scales a buffer from 44100hz to 8000hz (non-interpolated) */

  private short[] scaleBufferFreqDown44_1k(short inBuf[], short outBuf[]) {
    //8000 = 160 samples
    //44100 = 882 samples
    //ratio = 5.5125
    int outPos = 0, inPos = 0;
    int im = 0;
    for(int a=0;a<160;a++) {
      outBuf[outPos++] = inBuf[inPos];
      inPos += 5;
      im += 5125;
      if (im >= 10000) {
        inPos++;
        im -= 10000;
      }
    }
    return outBuf;
  }

  /** Scales a buffer from 32000hz to 8000hz (non-interpolated) */

  private short[] scaleBufferFreqDown32k(short inBuf[], short outBuf[]) {
    //8000 = 160 samples
    //32000 = 640 samples
    //ratio = 4.0
    int outPos = 0, inPos = 0;
    int im = 0;
    for(int a=0;a<160;a++) {
      outBuf[outPos++] = inBuf[inPos];
      inPos += 4;
    }
    return outBuf;
  }

  /** Writes data to the audio system (output to speakers). */

  private void write(short buf[]) {
    if (player == null) return;
    scaleBufferVolume(buf, 0, 160, volPlay);
    player.buffer.add(buf, 0, 160);
    synchronized(player.lock) {
      player.lock.notify();
    }
    int lvl = 0;
    for (int a = 0; a < 160; a++) {
      if (Math.abs(buf[a]) > lvl) lvl = Math.abs(buf[a]);
    }
    mc.setMeterPlay(lvl * 100 / 32768);
    if ((Settings.current.speakerMode) && (lvl >= Settings.current.speakerThreshold)) {
      if (speakerDelay <= 0) {
        mc.setSpeakerStatus(false);
      }
      speakerDelay = Settings.current.speakerDelay;
    }
  }

  /** Reads data from the audio system (input from mic). */

  private boolean read(short buf[]) {
    short dataDown[] = null;
    switch (sampleRate) {
      case 8000: dataDown = buf; break;
      case 32000: dataDown = dataDown32k; break;
      case 44100: dataDown = dataDown44_1k; break;
    }
    input.read(dataDown);
    switch (sampleRate) {
      case 32000: scaleBufferFreqDown32k(dataDown, buf); break;
      case 44100: scaleBufferFreqDown44_1k(dataDown, buf); break;
    }
    scaleBufferVolume(buf, 0, 160, volRec);
    int lvl = 0;
    for (int a = 0; a < 160; a++) {
      if (Math.abs(buf[a]) > lvl) lvl = Math.abs(buf[a]);
    }
    mc.setMeterRec(lvl * 100 / 32768);
    if (speakerDelay > 0) {
      speakerDelay -= 20;
      System.arraycopy(silence, 0, buf, 0, 160);
      if (speakerDelay <= 0) {
        mc.setSpeakerStatus(true);
      }
    }
    return true;
  }

  /** Timer event that is triggered every 20ms.  Processes playback / recording. */

  public void process() {
    //20ms timer
    //do playback
    if (timer == null) return;
    try {
      int cc = 0;  //conf count
      byte encoded[];
      if (!Settings.current.keepAudioOpen) {
        if (!playing) {
          for (int a = 0; a < 6; a++) {
            if ((lines[a].talking) || (lines[a].ringing)) {
              playing = true;
              output.start(1, sampleRate, 16, sampleRate50x2, Settings.current.audioOutput);
              write(silence);  //prime output
              break;
            }
          }
        } else {
          int pc = 0;  //playing count
          for (int a = 0; a < 6; a++) {
            if ((lines[a].talking) || (lines[a].ringing)) {
              pc++;
            }
          }
          if (pc == 0) {
            playing = false;
            mc.setMeterPlay(0);
            output.stop();
          }
        }
      }
      for (int a = 0; a < 6; a++) {
        if (lines[a].talking) {
          if ((lines[a].cnf) && (!lines[a].hld)) cc++;
        }
      }
      for (int a = 0; a < 6; a++) {
        if (lines[a].ringing && !lines[a].ringback && !lines[a].incoming) {
          if (!outRinging) {
            if (outWav != null) {
              outWav.reset();
            } else {
              startRinging();
            }
            outRinging = true;
          }
          break;
        }
        if (a == 5) {
          outRinging = false;
        }
      }
      for (int a = 0; a < 6; a++) {
        if (lines[a].incoming) {
          if (!inRinging) {
            if (inWav != null) {
              inWav.reset();
            } else {
              startRinging();
            }
            inRinging = true;
          }
          break;
        }
        if (a == 5) {
          inRinging = false;
        }
      }
      if ((cc > 1) && (line != -1) && (lines[line].cnf)) {
        //conference mode
        System.arraycopy(silence, 0, mixed, 0, 160);
        for (int a = 0; a < 6; a++) {
          if ((lines[a].talking) && (lines[a].audioRTP.getDefaultChannel().getSamples(lines[a].samples) && (lines[a].cnf) && (!lines[a].hld))) {
            mix(mixed, lines[a].samples);
          }
        }
        if (inRinging) mix(mixed, getCallWaiting());
        if (lines[line].dtmf != 'x') mix(mixed, dtmf.getSamples(lines[line].dtmf));
        write(mixed);
      } else {
        //single mode
        System.arraycopy(silence, 0, mixed, 0, 160);
        if (line != -1) {
          if (lines[line].dtmf != 'x') mix(mixed, dtmf.getSamples(lines[line].dtmf));
        }
        if ((line != -1) && (lines[line].talking) && (!lines[line].hld)) {
          if (lines[line].audioRTP.getDefaultChannel().getSamples(indata)) mix(mixed, indata);
          if (inRinging) mix(mixed, getCallWaiting());
          write(mixed);
        } else {
          if (inRinging || outRinging) mix(mixed, getRinging());
          if ((playing) || (Settings.current.keepAudioOpen)) write(mixed);
        }
      }
      if (record != null) System.arraycopy(mixed, 0, recording, 0, 160);
      //do recording
      boolean readstatus = read(outdata);
      if (!readstatus) JFLog.log("Sound:mic underbuffer");
      if ((mute) || (!readstatus)) System.arraycopy(silence, 0, outdata, 0, 160);
      for (int a = 0; a < 6; a++) {
        if ((lines[a].talking) && (!lines[a].hld)) {
          if (lines[a].ringback) {
            //send only silence during ringback
            encoded = lines[a].audioRTP.getDefaultChannel().coder.encode(silence);
          } else {
            if ((lines[a].cnf) && (cc > 1)) {
              //conference mode (mix = outdata + all other cnf lines except this one)
              System.arraycopy(outdata, 0, mixed, 0, 160);
              for (int b = 0; b < 6; b++) {
                if (b == a) continue;
                if ((lines[b].talking) && (lines[b].cnf) && (!lines[b].hld)) mix(mixed, lines[b].samples);
              }
              encoded = lines[a].audioRTP.getDefaultChannel().coder.encode(mixed);
              if (record != null) mix(recording, mixed);
            } else {
              //single mode
              if (line == a) {
                encoded = lines[a].audioRTP.getDefaultChannel().coder.encode(outdata);
                if (record != null) mix(recording, outdata);
              } else {
                encoded = lines[a].audioRTP.getDefaultChannel().coder.encode(silence);
              }
            }
          }
          if (lines[a].dtmfend) {
            lines[a].audioRTP.getDefaultChannel().writeDTMF(lines[a].dtmf, true);
          } else if (lines[a].dtmf != 'x') {
            lines[a].audioRTP.getDefaultChannel().writeDTMF(lines[a].dtmf, false);
          } else {
            lines[a].audioRTP.getDefaultChannel().writeRTP(encoded,0,encoded.length);
          }
        }
        if (lines[a].dtmfend) {
          lines[a].dtmfend = false;
          lines[a].dtmf = 'x';
        }
      }
      if (record != null) record.write(recording);  //file I/O - may need to move this out of 20ms timer
    } catch (Exception e) {
      JFLog.log(e);
    }
  }

  /** Mixes 'in' samples into 'out' samples. */

  public void mix(short out[], short in[]) {
    for (int a = 0; a < 160; a++) {
      out[a] += in[a];
    }
  }

  /** Starts a generated ringing phone sound. */

  private void startRinging() {
    outRingFreq1Count = 0;
    outRingFreq2Count = 0;
    outRingCycle = 0;
    outRingCount = 0;

    inRingFreq1Count = 0;
    inRingFreq2Count = 0;
    inRingCycle = 0;
    inRingCount = 0;

    waitCount = 0;
    waitCycle = 0;
  }

  private final double ringVol = 8000.0;

  private int outRingFreq1, outRingFreq2;
  private int outRingFreq1Count, outRingFreq2Count;
  private int outRingCycle;
  private int outRingCount;
  private int outRingTimes[] = new int[4];

  private int inRingFreq1, inRingFreq2;
  private int inRingFreq1Count, inRingFreq2Count;
  private int inRingCycle;
  private int inRingCount;
  private int inRingTimes[] = new int[4];

  private void initOutRinging(int freq1, int freq2, int o1, int p1, int o2, int p2) {
    outRingFreq1 = freq1;
    outRingFreq2 = freq2;
    outRingTimes[0] = o1;
    outRingTimes[1] = p1;
    outRingTimes[2] = o2;
    outRingTimes[3] = p2;
  }

  private void initInRinging(int freq1, int freq2, int o1, int p1, int o2, int p2) {
    inRingFreq1 = freq1;
    inRingFreq2 = freq2;
    inRingTimes[0] = o1;
    inRingTimes[1] = p1;
    inRingTimes[2] = o2;
    inRingTimes[3] = p2;
  }

  /** Returns next 20ms of ringing phone. */

  public short[] getRinging() {
    if (outRinging && outWav != null) {
      return outWav.getSamples();
    }
    if (inRinging && inWav != null) {
      return inWav.getSamples();
    }
    if (outRinging) {
      outRingCount += 20;
      if (outRingCount == outRingTimes[outRingCycle]) {
        outRingCount = 0;
        outRingCycle++;
        if (outRingCycle == 4) outRingCycle = 0;
      }
      if (outRingCycle % 2 == 1) {
        outRingFreq1Count = 0;
        outRingFreq2Count = 0;
        return silence;
      }
      //freq1
      for (int a = 0; a < 160; a++) {
        ringing[a] = (short) (Math.sin((2.0 * Math.PI / (8000.0 / outRingFreq1)) * (a + outRingFreq1Count)) * ringVol);
      }
      outRingFreq1Count += 160;
      if (outRingFreq1Count == 8000) outRingFreq1Count = 0;
      //freq2
      for (int a = 0; a < 160; a++) {
        ringing[a] += (short) (Math.sin((2.0 * Math.PI / (8000.0 / outRingFreq2)) * (a + outRingFreq2Count)) * ringVol);
      }
      outRingFreq2Count += 160;
      if (outRingFreq2Count == 8000) outRingFreq2Count = 0;
      return ringing;
    }
    if (inRinging) {
      inRingCount += 20;
      if (inRingCount == inRingTimes[inRingCycle]) {
        inRingCount = 0;
        inRingCycle++;
        if (inRingCycle == 4) inRingCycle = 0;
      }
      if (inRingCycle % 2 == 1) {
        inRingFreq1Count = 0;
        inRingFreq2Count = 0;
        return silence;
      }
      //freq1
      for (int a = 0; a < 160; a++) {
        ringing[a] = (short) (Math.sin((2.0 * Math.PI / (8000.0 / inRingFreq1)) * (a + inRingFreq1Count)) * ringVol);
      }
      inRingFreq1Count += 160;
      if (inRingFreq1Count == 8000) inRingFreq1Count = 0;
      //freq2
      for (int a = 0; a < 160; a++) {
        ringing[a] += (short) (Math.sin((2.0 * Math.PI / (8000.0 / inRingFreq2)) * (a + inRingFreq2Count)) * ringVol);
      }
      inRingFreq2Count += 160;
      if (inRingFreq2Count == 8000) inRingFreq2Count = 0;
      return ringing;
    }
    return silence;  //does not happen
  }

  private int waitCount;
  private int waitCycle;

  /** Returns next 20ms of a generated call waiting sound (beep beep). */

  public short[] getCallWaiting() {
    //440 (2 bursts for 0.3 seconds)
    //2on 2off 2on 200off[4sec]
    waitCycle++;
    if (waitCycle == 206) waitCycle = 0;
    if ((waitCycle > 6) || (waitCycle == 2) || (waitCycle == 3)) {
      waitCount = 0;
      return silence;
    }
    //440
    for (int a = 0; a < 160; a++) {
      callWaiting[a] = (short) (Math.sin((2.0 * Math.PI / (8000.0 / 440.0)) * (a + waitCount)) * ringVol);
    }
    waitCount += 160;
    if (waitCount == 8000) waitCount = 0;
    return callWaiting;
  }

  public Record record;

  private class Player extends Thread {
    private volatile boolean active = true;
    private volatile boolean done = false;
    public AudioBuffer buffer = new AudioBuffer(8000, 1, 2);  //freq, chs, seconds
    public final Object lock = new Object();
    public void run() {
      short buf[] = new short[160];
      short dataUp[] = null;
      switch (sampleRate) {
        case 8000: dataUp = buf; break;
        case 32000: dataUp = new short[640]; break;
        case 44100: dataUp = new short[882]; break;
      }
      while (active) {
        synchronized(lock) {
          if (buffer.size() < 160) {
            try { lock.wait(); } catch (Exception e) {}
          }
          if (buffer.size() < 160) continue;
        }
        buffer.get(buf, 0, 160);
        switch (sampleRate) {
          case 32000: {
            switch (Settings.current.interpolation) {
              case Settings.I_LINEAR: scaleBufferFreqUp32kLinear(buf, dataUp); break;
              case Settings.I_FILTER_CAP: scaleBufferFreqUp32kFilterCap(buf, dataUp); break;
            }
            break;
          }
          case 44100: {
            switch (Settings.current.interpolation) {
              case Settings.I_LINEAR: scaleBufferFreqUp44_1kLinear(buf, dataUp); break;
              case Settings.I_FILTER_CAP: scaleBufferFreqUp44_1kFilterCap(buf, dataUp); break;
            }
          }
        }
        output.write(dataUp);
      }
      done = true;
    }
    public void cancel() {
      active = false;
      while (!done) {
        synchronized(lock) {
          lock.notify();
        }
        JF.sleep(10);
      }
    }
  }
}
