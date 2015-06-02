/*

Records a line to a WAV file.

*/

import java.io.*;
import java.util.*;

import javaforce.*;

public class Record {
  private RandomAccessFile raf;
  private int length;
  private byte buf8[] = new byte[160 * 2];

  public void open() {
    try {
      length = 0;
      Calendar cal = Calendar.getInstance();
      String fn = String.format("%s/%d-%02d-%02d %02d-%02d-%02d.wav"
        , Settings.current.downloadPath
        , cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH)
        , cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
      raf = new RandomAccessFile(fn, "rw");
      writeHeader();
    } catch (Exception e) {
      JFLog.log(e);
    }
  }

  private void writeHeader() {
    byte header[] = new byte[44];
    header[0 + 0] = 'R';
    header[0 + 1] = 'I';
    header[0 + 2] = 'F';
    header[0 + 3] = 'F';
    LE.setuint32(header, 4, 36 + length*2);
    header[8 + 0] = 'W';
    header[8 + 1] = 'A';
    header[8 + 2] = 'V';
    header[8 + 3] = 'E';
    header[12 + 0] = 'f';
    header[12 + 1] = 'm';
    header[12 + 2] = 't';
    header[12 + 3] = ' ';
    LE.setuint32(header, 16, 16);  //chunk size
    LE.setuint16(header, 20, 1);   //1=PCM
    LE.setuint16(header, 22, 1);   //1=mono
    LE.setuint32(header, 24, 8000);  //freq
    LE.setuint32(header, 28, 8000 * 2);  //byte rate
    LE.setuint16(header, 32, 2);    //block align
    LE.setuint16(header, 34, 16);   //bits/sample
    header[36 + 0] = 'd';
    header[36 + 1] = 'a';
    header[36 + 2] = 't';
    header[36 + 3] = 'a';
    LE.setuint32(header, 40, length*2);  //data size
    try {
      raf.seek(0);
      raf.write(header);
    } catch (Exception e) {
      JFLog.log(e);
    }
  }

  //little endian format
  private byte[] short2byte(short in[], byte out[]) {
    for (int a = 0; a < 160; a++) {
      out[a * 2] = (byte) (in[a] & 0xff);
      out[a * 2 + 1] = (byte) (in[a] >>> 8);
    }
    return out;
  }

  public void write(short buf[]) {
    short2byte(buf, buf8);
    try {raf.write(buf8);} catch (Exception e) {JFLog.log(e);}
    length += 160;
    if (length > (24 * 60 * 60 * 8000)) {  //24 hrs (1.3 GBs)
      close();
      open();
    }
  }

  public void close() {
    if (raf == null) return;
    //patch header (length)
    writeHeader();
    try {
      raf.close();
    } catch (Exception e) {
      JFLog.log(e);
    }
    raf = null;
  }
}
