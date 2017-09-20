// code by jph
package ch.ethz.idsc.retina.dev.lidar.app;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.idsc.retina.dev.lidar.LidarRayDataListener;
import ch.ethz.idsc.retina.dev.lidar.LidarRotationEvent;
import ch.ethz.idsc.retina.dev.lidar.LidarRotationListener;

public abstract class LidarPanoramaProvider implements LidarRayDataListener, LidarRotationListener {
  private final List<LidarPanoramaListener> lidarPanoramaListeners = new LinkedList<>();
  protected LidarPanorama lidarPanorama;

  public LidarPanoramaProvider() {
    lidarPanorama = supply();
  }

  public final void addListener(LidarPanoramaListener lidarPanoramaListener) {
    lidarPanoramaListeners.add(lidarPanoramaListener);
  }

  @Override
  public final void timestamp(int usec, int type) {
    // ---
  }

  @Override
  public final void lidarRotation(LidarRotationEvent lidarRotationEvent) {
    lidarPanoramaListeners.forEach(listener -> listener.lidarPanorama(lidarPanorama));
    lidarPanorama = supply();
  }

  protected abstract LidarPanorama supply();
}