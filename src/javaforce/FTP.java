package javaforce;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.net.*;
import javax.net.ssl.*;

/**
 * FTP client class. Supports Passive and Active mode.
 */
public class FTP {

  public static interface ProgressListener {

    public void setProgress(int value);
  }

  public FTP() {
  }
  private Socket s;
  private InputStream is;
  private OutputStream os;
  private BufferedReader br;
  private boolean passive = true;
  private String host;  //passive host
  private int pasvport; //passive port
  private ServerSocket active;  //active socket
  private boolean log = false;
  private ProgressListener progress;
  private boolean aborted = false;
  public boolean debug = false;
  /**
   * Holds the repsonse strings from the last executed command
   */
  public String response[];

  public boolean connect(String host, int port) throws Exception {
    s = new Socket(host, port);
    is = s.getInputStream();
    br = new BufferedReader(new InputStreamReader(is));
    os = s.getOutputStream();
    this.host = host;
    getResponse();
    if (response[response.length - 1].startsWith("220")) {
      return true;
    }
    disconnect();  //not valid FTP site
    return false;
  }

  public boolean connectSSL(String host, int port) throws Exception {
    TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
      }
    };
    // Let us create the factory where we can set some parameters for the connection
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
//      SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();  //this method will only work with trusted certs
    SSLSocketFactory sslsocketfactory = (SSLSocketFactory) sc.getSocketFactory();  //this method will work with untrusted certs
    SSLSocket ssl = (SSLSocket) sslsocketfactory.createSocket(host, port);
    s = (Socket) ssl;
    is = s.getInputStream();
    br = new BufferedReader(new InputStreamReader(is));
    os = s.getOutputStream();
    this.host = host;
    getResponse();
    if (response[response.length - 1].startsWith("220")) {
      return true;
    }
    disconnect();  //not valid SMTP site
    return false;
  }

  public void abort() {
    aborted = true;
  }

  public void disconnect() throws Exception {
    if (s != null) {
      s.close();
    }
    s = null;
    is = null;
    os = null;
  }

  public void addProgressListener(ProgressListener progress) {
    this.progress = progress;
  }

  public void setLogging(boolean state) {
    log = state;
  }

  public void setPassiveMode(boolean mode) {
    passive = mode;
  }

  public boolean setBinary() throws Exception {
    cmd("type i");
    getResponse();
    if (!response[response.length - 1].startsWith("200")) {
      return false;
    }
    return true;
  }

  public boolean setAscii() throws Exception {
    cmd("type a");
    getResponse();
    if (!response[response.length - 1].startsWith("200")) {
      return false;
    }
    return true;
  }

  public boolean login(String user, String pass) throws Exception {
    cmd("user " + user);
    getResponse();
    if (!response[response.length - 1].startsWith("331")) {
      return false;
    }
    cmd("pass " + pass);
    getResponse();
    if (!response[response.length - 1].startsWith("230")) {
      return false;
    }
    return true;
  }

  public void logout() throws Exception {
    cmd("quit");
    getResponse();  //should be "221" but ignored
  }

  public void cmd(String cmd) throws Exception {
    if ((s == null) || (s.isClosed())) {
      throw new Exception("not connected");
    }
    if (log) {
      if (cmd.startsWith("pass ")) {
        JFLog.log("pass ****");
      } else {
        JFLog.log(cmd);
      }
    }
    cmd += "\r\n";
    os.write(cmd.getBytes());
  }

  public void get(String filename, String out) throws Exception {
    getPort();
    cmd("retr " + filename);
    FileOutputStream fos = new FileOutputStream(out);
    getData(fos);
    fos.close();
    getResponse();
  }

  public void get(String filename, OutputStream os) throws Exception {
    getPort();
    cmd("retr " + filename);
    getData(os);
    getResponse();
  }

  public InputStream getStart(String filename) throws Exception {
    getPort();
    cmd("retr " + filename);
    return getData();
  }

  public void getFinish() throws Exception {
    getResponse();
  }

  public void put(InputStream is, String filename) throws Exception {
    getPort();
    cmd("stor " + filename);
    putData(is);
    getResponse();
  }

  public void put(String in, String filename) throws Exception {
    getPort();
    cmd("stor " + filename);
    FileInputStream fis = new FileInputStream(in);
    putData(fis);
    fis.close();
    getResponse();
  }

  public OutputStream putStart(String filename) throws Exception {
    getPort();
    cmd("stor " + filename);
    return putData();
  }

  public void putFinish() throws Exception {
    getResponse();
  }

  public void cd(String path) throws Exception {
    cmd("cwd " + path);
    getResponse();
  }

  public void chmod(int mode, String path) throws Exception {
    cmd("site chmod " + Integer.toString(mode, 8) + " " + path);
    getResponse();
  }

  public void mkdir(String path) throws Exception {
    cmd("mkd " + path);
    getResponse();
  }

  public void rename(String oldpath, String newpath) throws Exception {
    cmd("rnfr " + oldpath);
    getResponse();
    cmd("rnto " + newpath);
    getResponse();
  }

  public void rm(String path) throws Exception {
    cmd("dele " + path);
    getResponse();
  }

  public void rmdir(String path) throws Exception {
    cmd("rmd " + path);
    getResponse();
  }

  public String ls(String path) throws Exception {
    getPort();
    cmd("dir " + path);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    getData(baos);
    getResponse();
    if (!response[response.length - 1].startsWith("226")) {
      throw new Exception("bad listing");
    }
    return baos.toString();
  }

  public String pwd() throws Exception {
    cmd("pwd");
    getResponse();
    int res = response.length - 1;
    String str = response[res];
    if (!str.startsWith("257")) {
      throw new Exception("pwd failed");
    }
    if ((str.charAt(4) == '\"') && (str.charAt(str.length() - 1) == '\"')) {
      return str.substring(5, str.length() - 1);
    } else {
      return str.substring(4);
    }
  }
  private final int BUFSIZ = 64 * 1024;

  private void getPort() throws Exception {
    if (passive) {
      cmd("pasv");
      getResponse();
      String str = response[response.length - 1];
      if (!str.startsWith("227")) {
        throw new Exception("pasv failed");
      }
      //227 Entering Passive Mode (ip,ip,ip,ip,PORTHI,PORTLO).
      String strs[] = str.split(",");
      if (strs.length != 6) {
        throw new Exception("pasv failed - bad response");
      }
      int hi = Integer.valueOf(strs[4]);
      int lo = Integer.valueOf(strs[5].substring(0, strs[5].indexOf(")")));
      pasvport = (hi << 8) + lo;
    } else {
      active = new ServerSocket();
      int hi = active.getLocalPort() >> 8;
      int lo = active.getLocalPort() & 0xff;
      cmd(s.getLocalAddress().getHostAddress().replaceAll("[.]", ",") + "," + hi + "," + lo);
      getResponse();
      String str = response[response.length - 1];
      if (!str.startsWith("200")) {
        throw new Exception("port failed");
      }
    }
  }

  private InputStream getData() throws Exception {
    aborted = false;
    Socket ds;
    if (passive) {
      ds = new Socket(host, pasvport);
    } else {
      ds = active.accept();
    }
    byte data[] = new byte[BUFSIZ];
    InputStream dis = ds.getInputStream();
    getResponse();
    if (!response[response.length - 1].startsWith("150")) {
      throw new Exception("bad get");
    }
    return dis;
  }

  private void getData(OutputStream os) throws Exception {
    aborted = false;
    Socket ds;
    if (passive) {
      ds = new Socket(host, pasvport);
    } else {
      ds = active.accept();
    }
    byte data[] = new byte[BUFSIZ];
    InputStream dis = ds.getInputStream();
    getResponse();
    if (!response[response.length - 1].startsWith("150")) {
      throw new Exception("bad get");
    }
    int read, total = 0;
    while (!ds.isClosed() && !aborted) {
      read = dis.read(data);
      if (read == -1) {
        break;
      }
      if (read > 0) {
        os.write(data, 0, read);
        total += read;
        if (progress != null) {
          progress.setProgress(total);
        }
      }
    }
    //read any remaining data left in buffers
    do {
      read = dis.read(data);
      if (read > 0) {
        os.write(data, 0, read);
        total += read;
        if (progress != null) {
          progress.setProgress(total);
        }
      }
    } while ((read > 0) && (!aborted));
  }

  private OutputStream putData() throws Exception {
    aborted = false;
    Socket ds;
    if (passive) {
      ds = new Socket(host, pasvport);
    } else {
      ds = active.accept();
    }
    byte data[] = new byte[BUFSIZ];
    OutputStream dos = ds.getOutputStream();
    getResponse();
    if (!response[response.length - 1].startsWith("150")) {
      throw new Exception("bad put");
    }
    return dos;
  }

  private void putData(InputStream is) throws Exception {
    aborted = false;
    Socket ds;
    if (passive) {
      ds = new Socket(host, pasvport);
    } else {
      ds = active.accept();
    }
    byte data[] = new byte[BUFSIZ];
    OutputStream dos = ds.getOutputStream();
    getResponse();
    if (!response[response.length - 1].startsWith("150")) {
      throw new Exception("bad put");
    }
    int total = 0;
    while ((!ds.isClosed()) && (is.available() > 0) && (!aborted)) {
      int read = is.read(data);
      if (read > 0) {
        dos.write(data, 0, read);
        total += read;
        if (progress != null) {
          progress.setProgress(total);
        }
      }
    }
    dos.flush();
    ds.close();
  }

  private void getResponse() throws Exception {
    ArrayList<String> tmp = new ArrayList<String>();
    String str;
    while (!s.isClosed()) {
      str = br.readLine();
      tmp.add(str);
      if (str.charAt(3) == ' ') {
        break;
      }
    }
    int size = tmp.size();
    response = new String[size];
    for (int a = 0; a < size; a++) {
      response[a] = tmp.get(a);
      if (debug) {
        System.out.println(response[a]);
      }
    }
  }
};
