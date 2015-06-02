package javaforce;

/**
 *
 * @author pquiring
 *
 * Created : Oct 9, 2013
 */

import java.awt.*;
import java.awt.datatransfer.*;

public class JFClipboard {
  public static class ImageTransferable implements Transferable
  {
    private Image image;

    public ImageTransferable (Image image)
    {
      this.image = image;
    }

    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException
    {
      if (isDataFlavorSupported(flavor))
      {
        return image;
      }
      else
      {
        throw new UnsupportedFlavorException(flavor);
      }
    }

    public boolean isDataFlavorSupported (DataFlavor flavor)
    {
      return flavor == DataFlavor.imageFlavor;
    }

    public DataFlavor[] getTransferDataFlavors ()
    {
      return new DataFlavor[] { DataFlavor.imageFlavor };
    }
  }

  public static Image readImage()
  {
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

    try
    {
      if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor))
      {
        Image image = (Image)t.getTransferData(DataFlavor.imageFlavor);
        return image;
      }
    }
    catch (Exception e) {
      JFLog.log(e);
    }

    return null;
  }

  /** Converts unknown Image format to JFImage. */
  public static JFImage convertImage(Image image) {
    int x = image.getWidth(null);
    int y = image.getHeight(null);
    JFImage jfimg = new JFImage(x,y);
    jfimg.getGraphics().drawImage(image, 0, 0, null);
    return jfimg;
  }

  public static void writeImage(Image image)
  {
    ImageTransferable transferable = new ImageTransferable(image);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
  }

  public static String readString() {
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    try
    {
      if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor))
      {
        String str = (String)t.getTransferData(DataFlavor.stringFlavor);
        return str;
      }
    }
    catch (Exception e) {
      JFLog.log(e);
    }

    return null;
  }

  public static void writeString(String str) {
    StringSelection ss = new StringSelection(str);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
  }
};
