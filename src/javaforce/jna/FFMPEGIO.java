package javaforce.jna;

/**
 * Interface callback for FFMPEG I/O
 *
 * @author pquiring
 *
 * Created : 8/13/2013
 */

public interface FFMPEGIO {
  public int read(FFMPEG.Coder coder, byte data[], int size);
  public int write(FFMPEG.Coder coder, byte data[]);
  public long seek(FFMPEG.Coder coder, long pos, int how);
}
