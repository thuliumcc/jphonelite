package javaforce.jna.com;

/**
 *
 * @author pquiring
 *
 * Created : Aug 18, 2013
 */

import com.sun.jna.*;

public class IGraphBuilder extends IFilterGraph {
  public static class ByReference extends IGraphBuilder implements Structure.ByReference {}
  public IGraphBuilder() {}
  public IGraphBuilder(Pointer pvInstance) {
    super(pvInstance);
  }
  //11 = Connect
  //12 = Render
  //13 = RenderFile
  //14 = AddSourceFilter
  //15 = SetLogFile
  //16 = Abort
  //17 = ShouldOperationContinue
}
