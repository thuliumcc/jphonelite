package javaforce.voip;

/** Secure RTP [*experimental*]
 *
 * @author pquiring
 *
 * Created : Dec ?, 2013
 *
 * RFCs:
 * http://tools.ietf.org/html/rfc3711 - SRTP
 * http://tools.ietf.org/html/rfc5764 - Using DTLS to exchange keys for SRTP
 * http://tools.ietf.org/html/rfc4568 - SDP w/ SRTP keys (old method before DTLS)
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.security.*;
import java.util.*;
import javax.crypto.Mac;

import javaforce.*;

import org.bouncycastle.crypto.tls.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.asn1.pkcs.*;
import org.bouncycastle.crypto.util.*;

public class SRTPChannel extends RTPChannel {
  protected SRTPChannel(RTP rtp, int ssrc, SDP.Stream stream) {
    super(rtp,ssrc,stream);
  }
  private boolean have_keys = false;
  private boolean dtls = false;
  private boolean dtlsServerMode = false;
  private boolean stunReceived = false;
  private SRTPContext srtp_in, srtp_out;
  private int _tailIn, _tailOut;
  private long _seqno = 0;  //must keep track of seqno beyond 16bits

  public void writeRTP(byte data[], int off, int len) {
    if (srtp_out == null) {
      if (!have_keys) return;  //not ready
      try {
        srtp_out = new SRTPContext();
        srtp_out.setCrypto("AES_CM_128_HMAC_SHA1_80", serverKey, serverSalt);
        _tailOut = srtp_out.getAuthTail();
        srtp_out.deriveKeys(0);
      } catch (Exception e) {
        JFLog.log(e);
        return;
      }
    }
    try {
      byte payload[] = Arrays.copyOfRange(data, off+12, off + len);
      int ssrc = BE.getuint32(data, off + 8);
//      int stamp = BE.getuint32(data, off + 4);
      int seqno = BE.getuint16(data, off + 2);
      if (stream.keyExchange == SDP.KeyExchange.SDP) {
//        srtp_out.deriveKeys(stamp);  //not used in RFC 5764 (RFC 3711:only needed if kdr != 0)
      }
      encrypt(payload, ssrc, _seqno++);
      byte packet[] = new byte[len + _tailOut];
      System.arraycopy(data, off, packet, 0, 12);
      System.arraycopy(payload, 0, packet, 12, payload.length);
      appendAuth(packet, srtp_out, seqno);
      super.writeRTP(packet, 0, packet.length);
    } catch (Exception e) {
      JFLog.log(e);
    }
  }

  /** Sets keys found in SDP used on this side of SRTP (not used in DTLS mode) */
  public void setServerKeys(byte key[], byte salt[]) {
    System.arraycopy(key, 0, serverKey, 0, 16);
    System.arraycopy(salt, 0, serverSalt, 0, 14);
    have_keys = true;
  }

  /** Sets keys found in SDP used on other side of SRTP (not used in DTLS mode) */
  public void setClientKeys(byte key[], byte salt[]) {
    System.arraycopy(key, 0, clientKey, 0, 16);
    printArray("client  key=", key, 0, 16);
    System.arraycopy(salt, 0, clientSalt, 0, 14);
    printArray("client salt=", salt, 0, 14);
    have_keys = true;
  }

  /** Enables DTLS mode (otherwise you MUST call setServerKeys() AND setClientKeys() before calling start()). */
  public void setDTLS(boolean server) {
    dtls = true;
    dtlsServerMode = server;
  }

  public boolean start() {
    if (!super.start()) return false;
    if (!dtls) return have_keys;
    //create a thread to do STUN/DTLS request
    new Thread() {
      public void run() {
        while (!stunReceived) {
          JF.sleep(500);
          Random r = new Random();
          byte request[] = new byte[1500];
          ByteBuffer bb = ByteBuffer.wrap(request);
          bb.order(ByteOrder.BIG_ENDIAN);
          int offset = 0;
          bb.putShort(offset, BINDING_REQUEST);
          offset += 2;
          int lengthOffset = offset;
          bb.putShort(offset, (short)0);  //length (patch later)
          offset += 2;
          long id1;
          id1 = 0x2112a442;  //magic cookie
          id1 <<= 32;
          id1 += Math.abs(r.nextInt());
          bb.putLong(offset, id1);
          offset += 8;
          long id2 = r.nextLong();
          bb.putLong(offset, id2);
          offset += 8;

          String user = stream.sdp.iceufrag + ":" + "12345678";
          int strlen = user.length();
          bb.putShort(offset, USERNAME);
          offset += 2;
          bb.putShort(offset, (short)strlen);
          offset += 2;
          System.arraycopy(user.getBytes(), 0, request, offset, strlen);
          offset += strlen;
          if ((offset & 3) > 0) {
            offset += 4 - (offset & 3);  //padding
          }

          //ICE:PRIORITY (MUST)
          bb.putShort(offset, PRIORITY);
          offset += 2;
          bb.putShort(offset, (short)4);
          offset += 2;
          bb.putInt(offset, Math.abs(r.nextInt()));  //TODO : calc this???
          offset += 4;

          //ICE:ICE_CONTROLLED (MUST)
          bb.putShort(offset, ICE_CONTROLLED);
          offset += 2;
          bb.putShort(offset, (short)8);
          offset += 2;
          bb.putLong(offset, r.nextLong());  //random tie-breaker
          offset += 8;

          bb.putShort(lengthOffset, (short)(offset - 20 + 24));  //patch length (24=MSG_INT)

          JFLog.log("ice password=" + stream.sdp.icepwd);
          byte id[] = STUN.calcMsgIntegrity(request, offset, STUN.calcKey(stream.sdp.icepwd));
          strlen = id.length;
          bb.putShort(offset, MESSAGE_INTEGRITY);
          offset += 2;
          bb.putShort(offset, (short)strlen);
          offset += 2;
          System.arraycopy(id, 0, request, offset, strlen);
          offset += strlen;
          if ((offset & 3) > 0) {
            offset += 4 - (offset & 3);  //padding
          }

          bb.putShort(lengthOffset, (short)(offset - 20 + 8));  //patch length (8=FINGERPRINT)

          //fingerprint
          bb.putShort(offset, FINGERPRINT);
          offset += 2;
          bb.putShort(offset, (short)4);
          offset += 2;
          bb.putInt(offset, STUN.calcFingerprint(request, offset - 4));
          offset += 4;

          bb.putShort(lengthOffset, (short)(offset - 20));  //patch length

          try {
            if (rtp.useTURN) {
              rtp.stun1.sendData(turn1ch, request, 0, offset);
            } else {
              DatagramPacket dp = new DatagramPacket(request, 0, offset, InetAddress.getByName(stream.getIP()), stream.getPort());
              rtp.sock1.send(dp);
            }
          } catch (Exception e) {
            JFLog.log(e);
          }
        }

        if (!dtlsServerMode) {
          //TODO:DTLS client code (other side should do it for now)
        }
      }
    }.start();
    return true;
  }
  private final static short BINDING_REQUEST = 0x0001;

  private final static short BINDING_RESPONSE = 0x0101;

  private final static short MAPPED_ADDRESS = 0x0001;
  private final static short USERNAME = 0x0006;
  private final static short XOR_MAPPED_ADDRESS = 0x0020;
  private final static short MESSAGE_INTEGRITY = 0x0008;

  //http://tools.ietf.org/html/rfc5245 - ICE
  private final static short USE_CANDIDATE = 0x25;  //used by ICE_CONTROLLING only
  private final static short PRIORITY = 0x24;  //see section 4.1.2
  private final static short ICE_CONTROLLING = (short)0x802a;
  private final static short ICE_CONTROLLED = (short)0x8029;
  private final static short FINGERPRINT = (short)0x8028;

  private byte stun[];

  private boolean isClassicStun(long id1) {
    return ((id1 >>> 32) != 0x2112a442);
  }

  private int IP4toInt(String ip) {
    String o[] = ip.split("[.]");
    int ret = 0;
    for (int a = 0; a < 4; a++) {
      ret <<= 8;
      ret += (JF.atoi(o[a]));
    }
    return ret;
  }

  protected void processSTUN(byte data[], int off, int len) {
    //the only command supported is BINDING_REQUEST
    String username = null, flipped = null;
    ByteBuffer bb = ByteBuffer.wrap(data, off, len);
    bb.order(ByteOrder.BIG_ENDIAN);
    int offset = off;
    short code = bb.getShort(offset);
    boolean auth = false;
    if (code != BINDING_REQUEST) {
      JFLog.log("RTPSecureChannel:Error:STUN Request is not Binding request");
      return;
    }
    offset += 2;
    int lengthOffset = offset;
    short length = bb.getShort(offset);
    offset += 2;
    long id1 = bb.getLong(offset);
    offset += 8;
    long id2 = bb.getLong(offset);
    offset += 8;
    while (offset < len) {
      short attr = bb.getShort(offset);
      offset += 2;
      length = bb.getShort(offset);
      offset += 2;
      switch (attr) {
        case USERNAME:
          username = new String(data, offset, length);
          String f[] = username.split("[:]");
          flipped = f[1] + ":" + f[0];  //reverse username
          break;
        case MESSAGE_INTEGRITY:
          bb.putShort(lengthOffset, (short) (offset));  //patch length
          byte correct[] = STUN.calcMsgIntegrity(data, offset - 4, STUN.calcKey("javaforce"));
          byte supplied[] = Arrays.copyOfRange(data, offset, offset + 20);
          auth = Arrays.equals(correct, supplied);
          break;
      }
      offset += length;
      if ((length & 0x3) > 0) {
        offset += 4 - (length & 0x3);  //padding
      }
    }
    if (!auth) {
      return;  //wrong credentials
    }
    //build response
    if (stun == null) {
      stun = new byte[1500];
    }
    bb = ByteBuffer.wrap(stun);
    bb.order(ByteOrder.BIG_ENDIAN);
    offset = 0;
    bb.putShort(offset, BINDING_RESPONSE);
    offset += 2;
    lengthOffset = offset;
    bb.putShort(offset, (short) 0);  //length (patch later)
    offset += 2;
    bb.putLong(offset, id1);
    offset += 8;
    bb.putLong(offset, id2);
    offset += 8;

    if (isClassicStun(id1)) {
      bb.putShort(offset, MAPPED_ADDRESS);
      offset += 2;
      bb.putShort(offset, (short) 8);  //length of attr
      offset += 2;
      bb.put(offset, (byte) 0);  //reserved
      offset++;
      bb.put(offset, (byte) 1);  //IP family
      offset++;
      bb.putShort(offset, (short) stream.port);
      offset += 2;
      bb.putInt(offset, IP4toInt(stream.getIP()));
      offset += 4;
    } else {
      //use XOR_MAPPED_ADDRESS instead
      bb.putShort(offset, XOR_MAPPED_ADDRESS);
      offset += 2;
      bb.putShort(offset, (short) 8);  //length of attr
      offset += 2;
      bb.put(offset, (byte) 0);  //reserved
      offset++;
      bb.put(offset, (byte) 1);  //IP family
      offset++;
      bb.putShort(offset, (short) (stream.port ^ bb.getShort(4)));
      offset += 2;
      bb.putInt(offset, IP4toInt(stream.getIP()) ^ bb.getInt(4));
      offset += 4;
    }

    bb.putShort(lengthOffset, (short) (offset - 20 + 24));  //patch length
    byte id[] = STUN.calcMsgIntegrity(stun, offset, STUN.calcKey("javaforce"));
    int strlen = id.length;
    bb.putShort(offset, MESSAGE_INTEGRITY);
    offset += 2;
    bb.putShort(offset, (short) strlen);
    offset += 2;
    System.arraycopy(id, 0, stun, offset, strlen);
    offset += strlen;
    if ((offset & 3) > 0) {
      offset += 4 - (offset & 3);  //padding
    }

    bb.putShort(lengthOffset, (short) (offset - 20));  //patch length

    try {
      if (rtp.useTURN) {
        rtp.stun1.sendData(turn1ch, stun, 0, offset);
      } else {
        rtp.sock1.send(new DatagramPacket(stun, 0, offset, InetAddress.getByName(stream.getIP()), stream.getPort()));
      }
    } catch (Exception e) {
      JFLog.log(e);
    }
  }

  private static DTLSServerProtocol dtlsServer;
  private static Object dtlsLock = new Object();
  private static InetAddress localhost;
  private static org.bouncycastle.crypto.tls.Certificate dtlsCertChain;
  private static AsymmetricKeyParameter dtlsPrivateKey;

  public static boolean initDTLS(java.util.List<byte []> certChain, byte privateKey[], boolean pkRSA) {
    try {
      org.bouncycastle.asn1.x509.Certificate x509certs[] = new org.bouncycastle.asn1.x509.Certificate[certChain.size()];
      for (int i = 0; i < certChain.size(); ++i) {
        x509certs[i] = org.bouncycastle.asn1.x509.Certificate.getInstance(certChain.get(i));
      }
      dtlsCertChain = new org.bouncycastle.crypto.tls.Certificate(x509certs);
      if (pkRSA) {
        RSAPrivateKey rsa = RSAPrivateKey.getInstance(privateKey);
        dtlsPrivateKey = new RSAPrivateCrtKeyParameters(rsa.getModulus(), rsa.getPublicExponent(),
          rsa.getPrivateExponent(), rsa.getPrime1(), rsa.getPrime2(), rsa.getExponent1(),
          rsa.getExponent2(), rsa.getCoefficient());
      } else {
        dtlsPrivateKey = PrivateKeyFactory.createKey(privateKey);
      }
      return true;
    } catch (Exception e) {
      JFLog.log(e);
      return false;
    }
  }

  private class DefaultTlsServer2 extends DefaultTlsServer {
    //TODO : call this once I know myself that it's available (for now I wait for other side to send first SRTP packet)
    public void getKeys() {
      sharedSecret = context.exportKeyingMaterial(ExporterLabel.dtls_srtp, null, (KEY_LENGTH + SALT_LENGTH) * 2);
//      printArray("secret=", sharedSecret, 0, sharedSecret.length);
      int offset = 0;
      System.arraycopy(sharedSecret, offset, clientKey, 0, KEY_LENGTH);
      offset += KEY_LENGTH;
      System.arraycopy(sharedSecret, offset, serverKey, 0, KEY_LENGTH);
      offset += KEY_LENGTH;
      System.arraycopy(sharedSecret, offset, clientSalt, 0, SALT_LENGTH);
      offset += SALT_LENGTH;
      System.arraycopy(sharedSecret, offset, serverSalt, 0, SALT_LENGTH);
      offset += SALT_LENGTH;
      have_keys = true;
    }
  }

  private DatagramSocket dtlsSocket, rawSocket;
  private DefaultTlsServer2 tlsServer;
  private DTLSTransport dtlsTransport;
  private Worker worker;

  private byte[] sharedSecret;
  private byte[] clientKey = new byte[KEY_LENGTH], clientSalt = new byte[SALT_LENGTH+2];
  private byte[] serverKey = new byte[KEY_LENGTH], serverSalt = new byte[SALT_LENGTH+2];

  private static final int KEY_LENGTH = 16;
  private static final int SALT_LENGTH = 14;

  protected void processDTLS(byte data[], int off, int len) {
    ByteBuffer bb = ByteBuffer.wrap(data, off, len);
    bb.order(ByteOrder.BIG_ENDIAN);
    synchronized (dtlsLock) {
      if (dtlsServer == null) {
        try {
          dtlsServer = new DTLSServerProtocol(new SecureRandom());
          localhost = InetAddress.getByName("localhost");
        } catch (Exception e) {
          JFLog.log(e);
          dtlsServer = null;
          return;
        }
      }
    }
    if (dtlsSocket == null) {
      try {
        tlsServer = new DefaultTlsServer2() {
          public void notifyClientCertificate(org.bouncycastle.crypto.tls.Certificate clientCertificate)
                  throws IOException {
            org.bouncycastle.asn1.x509.Certificate[] chain = clientCertificate.getCertificateList();
//            JFLog.log("Received client certificate chain of length " + chain.length);
            for (int i = 0; i != chain.length; i++) {
              org.bouncycastle.asn1.x509.Certificate entry = chain[i];
//              JFLog.log("fingerprint:SHA-256 " + KeyMgmt.fingerprintSHA256(entry.getEncoded()) + " (" + entry.getSubject() + ")");
//              JFLog.log("cert length=" + entry.getEncoded().length);
            }
          }

          protected ProtocolVersion getMaximumVersion() {
            return ProtocolVersion.DTLSv12;
          }

          protected ProtocolVersion getMinimumVersion() {
            return ProtocolVersion.DTLSv10;
          }

          protected TlsEncryptionCredentials getRSAEncryptionCredentials()
            throws IOException
          {
            return new DefaultTlsEncryptionCredentials(context, dtlsCertChain, dtlsPrivateKey);
          }

          protected TlsSignerCredentials getRSASignerCredentials()
                  throws IOException {
            SignatureAndHashAlgorithm signatureAndHashAlgorithm = null;
            Vector sigAlgs = supportedSignatureAlgorithms;
            if (sigAlgs != null) {
              for (int i = 0; i < sigAlgs.size(); ++i) {
                SignatureAndHashAlgorithm sigAlg = (SignatureAndHashAlgorithm) sigAlgs.elementAt(i);
                if (sigAlg.getSignature() == SignatureAlgorithm.rsa) {
                  signatureAndHashAlgorithm = sigAlg;
                  break;
                }
              }

              if (signatureAndHashAlgorithm == null) {
                return null;
              }
            }
            return new DefaultTlsSignerCredentials(context, dtlsCertChain, dtlsPrivateKey, signatureAndHashAlgorithm);
          }

          public Hashtable getServerExtensions() throws IOException {
            //see : http://bouncy-castle.1462172.n4.nabble.com/DTLS-SRTP-with-bouncycastle-1-49-td4656286.html
            Hashtable table = super.getServerExtensions();
            if (table == null) table = new Hashtable();
            int[] protectionProfiles = {
// TODO : need to pick ONE that client offers
              SRTPProtectionProfile.SRTP_AES128_CM_HMAC_SHA1_80  //this is the only one supported for now
//              SRTPProtectionProfile.SRTP_AES128_CM_HMAC_SHA1_32
//              SRTPProtectionProfile.SRTP_NULL_HMAC_SHA1_32
//              SRTPProtectionProfile.SRTP_NULL_HMAC_SHA1_80
            };
            byte mki[] = new byte[0];  //should match client or use nothing
            UseSRTPData srtpData = new UseSRTPData(protectionProfiles, mki);
            TlsSRTPUtils.addUseSRTPExtension(table, srtpData);
            return table;
          }
        };

        dtlsSocket = new DatagramSocket(RTP.getnextlocalrtpport());
        rawSocket = new DatagramSocket(RTP.getnextlocalrtpport());
        dtlsSocket.connect(localhost, rawSocket.getLocalPort());
        rawSocket.connect(localhost, dtlsSocket.getLocalPort());
        new Thread() {
          public void run() {
            try {
              dtlsTransport = dtlsServer.accept(tlsServer, new UDPTransport(dtlsSocket, 1500 - 20 - 8));
            } catch (Exception e) {
              JFLog.log(e);
            }
          }
        }.start();
        worker = new Worker();
        worker.start();
      } catch (Exception e) {
        JFLog.log(e);
        dtlsSocket = null;
        rawSocket = null;
        return;
      }
    }
    try {
      rawSocket.send(new DatagramPacket(data, off, len, localhost, dtlsSocket.getLocalPort()));
    } catch (Exception e) {
      JFLog.log(e);
    }
  }


  private class Worker extends Thread {
    public void run() {
      try {
        int pcnt = 0;
        byte data[] = new byte[1500];
        while (active) {
          DatagramPacket pack = new DatagramPacket(data, 1500);
          rawSocket.receive(pack);
          int len = pack.getLength();
          int off = 0;
          if (rtp.useTURN) {
            rtp.stun1.sendData(turn1ch, data, off, len);
          } else {
            rtp.sock1.send(new DatagramPacket(data, off, len, InetAddress.getByName(stream.getIP()), stream.getPort()));
          }
          if (!have_keys) {
            pcnt++;
            if (pcnt > 1) {
              tlsServer.getKeys();
            }
          }
        }
      } catch (Exception e) {
        JFLog.log(e);
      }
    }
  }

  protected void processRTP(byte data[], int off, int len) {
    int firstByte = ((int)data[off]) & 0xff;
    //see http://tools.ietf.org/html/rfc5764#section-5
    if (firstByte == 0) {
      //STUN request
      processSTUN(data, off, len);
      return;
    }
    if (firstByte == 1) {
      //STUN response (contents not used)
      stunReceived = true;
      return;
    }
    if (firstByte > 127 && firstByte < 192) {
      //SRTP data
      if (!have_keys) {
        if (dtls) {
          tlsServer.getKeys();
        } else {
          JFLog.log("SRTPChannel:Error:Received SRTP data with no keys defined");
          return;
        }
      }
      if (srtp_in == null) {
        try {
          srtp_in = new SRTPContext();
          srtp_in.setCrypto("AES_CM_128_HMAC_SHA1_80", clientKey, clientSalt);
          _tailIn = srtp_in.getAuthTail();
          srtp_in.deriveKeys(0);
        } catch (Exception e) {
          JFLog.log(e);
        }
      }
      byte payload[] = Arrays.copyOfRange(data, off + 12, off + len);
      int seqno = BE.getuint16(data, off + 2);
      int ssrc = BE.getuint32(data, off + 8);
      try {
        decrypt(payload, ssrc, seqno);
      } catch (Exception e) {
        JFLog.log(e);
      }
      updateCounters(seqno);
      System.arraycopy(payload, 0, data, off+12, payload.length);
      super.processRTP(data, off, len - _tailIn);
      return;
    }
    //raw DTLS data (handshaking)
    if (dtls) processDTLS(data, off, len);
  }

  private void printArray(String msg, byte data[], int off, int len) {
    StringBuilder sb = new StringBuilder();
    for(int a=0;a<len;a++) {
      sb.append(",");
      sb.append(Integer.toString(((int)data[off + a]) & 0xff, 16));
    }
    JFLog.log(msg + "(" + len + ")=" + sb.toString());
  }

  private ByteBuffer getPepper(int ssrc, long idx) {
    //(SSRC * 2^64) XOR (i * 2^16)
    ByteBuffer pepper = ByteBuffer.allocate(16);
    pepper.putInt(4, ssrc);
    long sindex = idx << 16;
    pepper.putLong(8, sindex);

    return pepper;
  }

  private void decrypt(byte[] payload, int ssrc, int seqno) throws GeneralSecurityException {
    ByteBuffer in = ByteBuffer.wrap(payload);
    // aes likes the buffer a multiple of 32 and longer than the input.
    int pl = (((payload.length / 32) + 2) * 32);
    ByteBuffer out = ByteBuffer.allocate(pl);
    ByteBuffer pepper = getPepper(ssrc, getIndex(seqno));
    srtp_in.decipher(in, out, pepper);
    System.arraycopy(out.array(), 0, payload, 0, payload.length);
  }

  private void encrypt(byte[] payload, int ssrc, long idx) throws GeneralSecurityException {
    ByteBuffer in = ByteBuffer.wrap(payload);
    int pl = (((payload.length / 32) + 2) * 32);
    ByteBuffer out = ByteBuffer.allocate(pl);
    ByteBuffer pepper = getPepper(ssrc, idx);
    srtp_out.decipher(in, out, pepper);
    System.arraycopy(out.array(), 0, payload, 0, payload.length);
  }

  void appendAuth(byte[] packet, SRTPContext sc, int seqno) {
    try {
      // strictly we might need to derive the keys here too -
      // since we might be doing auth but no crypt.
      // we don't support that so nach.
      Mac mac = sc.getAuthMac();
      int offs = packet.length - _tailOut;
      ByteBuffer m = ByteBuffer.allocate(offs + 4);
      m.put(packet, 0, offs);
      int oroc = (int) (seqno >>> 16);
      m.putInt(oroc);
      m.position(0);
      mac.update(m);
      byte[] auth = mac.doFinal();
      for (int i = 0; i < _tailOut; i++) {
        packet[offs + i] = auth[i];
      }
    } catch (Exception e) {
      JFLog.log(e);
    }
  }

  protected long _roc = 0; // only used for inbound we _know_ the answer for outbound.
  //roc = rollover counter (counts everytime seqno(16bit) rolls over)
  protected int _s_l = 0;  // only used for inbound we _know_ the answer for outbound.
  //s_l = seqno last seen

  long getIndex(int seqno) {
    long v = _roc; // default assumption

    // detect wrap(s)
    int diff = seqno - _s_l; // normally we expect this to be 1
    if (diff < Short.MIN_VALUE) {
        // large negative offset so
        v = _roc + 1; // if the old value is more than 2^15 smaller
        // then we have wrapped
    }
    if (diff > Short.MAX_VALUE) {
        // big positive offset
        v = _roc - 1; // we  wrapped recently and this is an older packet.
    }
    if (v < 0) {
        v = 0; // trap odd initial cases
    }
    /*
    if (_s_l < 32768) {
    v = ((seqno - _s_l) > 32768) ? (_roc - 1) % (1 << 32) : _roc;
    } else {
    v = ((_s_l - 32768) > seqno) ? (_roc + 1) % (1 << 32) : _roc;
    }*/
    long low = (long) seqno;
    long high = ((long) v << 16);
    long ret = low | high;
    return ret;
  }

  void updateCounters(int seqno) {
    // note that we have seen it.
    int diff = seqno - _s_l; // normally we expect this to be 1
    if (diff < Short.MIN_VALUE) {
      // large negative offset so
      _roc++; // if the old value is more than 2^15 smaller
      // then we have wrapped
    }
    _s_l = seqno;
  }


}

/*

Data Flowcharts:

RTP Flow (0x80):
media <---> SRTPSecContext <---> rtpSocket

STUN Flow (0x00 or 0x01):
stun <---> realSocket

DTLS Flow (*):
dtlsSocket <---> rawSocket <---> rtpSocket

*/
