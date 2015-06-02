package javaforce.media;

/**
 * Common Camera (Native Code only)
 *
 * @author pquiring
 *
 * Created : Aug 20, 2013
 */

import javaforce.*;
import javaforce.jna.*;

public class Camera {
  public static interface Input {
    public boolean init();
    public boolean uninit();
    public String[] listDevices();
    public boolean start(int deviceIdx, int width, int height);
    public boolean stop();
    public int[] getFrame();
    public int getWidth();
    public int getHeight();
  }
  public static Input getInput() {
    if (JF.isWindows()) {
      return new WinCamera();
    } else {
/*  //not working yet!!!
      if (FFMPEG.init()) {
        return new FFMPEG.V4L2Camera();
      }*/
      return new LnxCamera();  //not that many pixelformat's supported
    }
  }
}
