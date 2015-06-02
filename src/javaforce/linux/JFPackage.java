package javaforce.linux;

/** PAR Package data
 *
 * @author pquiring
 */

public class JFPackage implements java.io.Serializable {
  public static final long serialVersionUID = 1L;
  public String name;
  public String version, arch;
  public String filename;
  public String[] depends = new String[0];
  public String[] files = new String[0];  //only if installed
  public int state; //bitwise flags
  //following fields are reserved (currently not used)
  public String desc, installSize, compressedSize, md5, extra[];

  //state flags
  public static int S_NONE = 0;
  public static int S_CONTROL_FILES = 1;
  public static int S_DATA_FILES = 2;
  public static int S_PREINST = 4;
  public static int S_POSTINST = 8;
  public static int S_DEPENDANT = 16;  //autoremove can remove
  public static int S_SYSTEM = 32;  //system package (do NOT remove)
}
