package javaforce.linux;

/** repository site data
 *
 * @author pquiring
 *
 * Created : Feb 27, 2014
 */

import java.util.*;

public class JFRepo implements java.io.Serializable {
  public static final long serialVersionUID = 1L;
  public String name;
  public String desc;
  public String mirrorsurl, baseurl, keyurl;
  public int priority = 0;  //0-127 (0=lowest - 127=highest)
  public boolean enabled = true;
  public ArrayList<JFPackage> packages;
  public String extra[];

  public transient ArrayList<String> mirrors;
}
