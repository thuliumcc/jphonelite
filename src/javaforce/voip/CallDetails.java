package javaforce.voip;

/**
 * Keeps track of Call Details based on the 'callid' field of SIP messages.
 */

import java.net.*;

public class CallDetails {

  /**
   * Every call has 2 sides, originator and terminator.
   */
  public static class SideDetails {
    public int cseq = 0;
    public int expires = 0;
    public String to[], from[];  //0=name < 1=# 2=host/port ... > ':' ...
    public String uri;
    public String contact;
    public String vialist[];
    public String branch;
    public String extra;   //extra headers
    public String epass;   //response to an 403/407 MD5
    public long o1, o2;
    public SDP sdp;  //sdp as decoded from inbound packets
    public String host;
    public InetAddress addr;
    public int port;
  };
  /**
   * Keeps track of details from source side (call originator)
   */
  public SideDetails src = new SideDetails();
  /**
   * Keeps track of details from destination side (call terminator)
   */
  public SideDetails dst = new SideDetails();
  public String callid;  //unique id for this call leg (not caller ID)
  public String sdp;  //sdp content to be added to outbound packets
  public String authstr;
  public boolean authsent;  //was auth (401/407) tried
}
