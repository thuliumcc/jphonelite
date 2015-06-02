package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 19, 2013
 */

import com.sun.jna.*;

public class IFilterGraph extends IUnknown {
  public static class ByReference extends IFilterGraph implements Structure.ByReference {}
  public IFilterGraph() {}
  public IFilterGraph(Pointer pvInstance) {
    super(pvInstance);
  }
  //3 = AddFilter
  public int AddFilter(Pointer iBaseFilter, WString pName) {
    Pointer _this = this.getPointer();
    Pointer vtbl = _this.getPointer(0);
    Pointer pfunc = vtbl.getPointer(3 * Pointer.SIZE);
    Function func = Function.getFunction(pfunc);
    return func.invokeInt(new Object[] { _this, iBaseFilter, pName });
  }
  //4 = RemoveFilter
  //5 = EnumFilters
  //6 = FindFilterByName
  //7 = ConnectDirect
  //8 = Reconnect
  //9 = Disconnect
  //10 = SetDefaultSyncSource
}
