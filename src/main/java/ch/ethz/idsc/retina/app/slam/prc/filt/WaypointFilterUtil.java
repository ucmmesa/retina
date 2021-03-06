// code by mg
package ch.ethz.idsc.retina.app.slam.prc.filt;

import java.util.Arrays;
import java.util.List;

import ch.ethz.idsc.retina.app.slam.SlamPrcContainer;

/** handles the list of way point filters used in SlamWaypointFilter */
public enum WaypointFilterUtil {
  ;
  public static List<WaypointFilterInterface> getWaypointFilters(SlamPrcContainer slamPrcContainer) {
    return Arrays.asList( //
        new RegionOfInterestFilter(), //
        new MergeWaypointsFilter(), //
        new SausageFilter(slamPrcContainer), //
        new CurvatureFilter());
  }
}
