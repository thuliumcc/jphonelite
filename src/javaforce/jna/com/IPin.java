package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

public class IPin extends IUnknown {
  public static class ByReference extends IPin implements Structure.ByReference {}
  public IPin() {}
  public IPin(Pointer pvInstance) {
    super(pvInstance);
  }
  //3 = Connect
  //4 = ReceiveConnection
  //5 = Disconnect
  //6 = ConnectedTo
  //7 = ConnectionMediaType
  //8 = QueryPinInfo
  //9 = QueryDirection
  //10 = QueryId
  //11 = QueryAccept
  //12 = EnumMediaTypes
  public int EnumMediaTypes(PointerByReference ref) {
    return invokeInt(12, new Object[] { getPointer(), ref});
  }
  //13 = QueryInternalConnections
  //14 = EndOfStream
  //15 = BeginFlush
  //16 = EndFlush
  //17 = NewSegment
}
