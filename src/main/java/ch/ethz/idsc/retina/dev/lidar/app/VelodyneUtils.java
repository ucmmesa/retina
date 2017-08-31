// code by jph
package ch.ethz.idsc.retina.dev.lidar.app;

import ch.ethz.idsc.retina.dev.lidar.LidarAngularFiringCollector;
import ch.ethz.idsc.retina.dev.lidar.LidarRotationProvider;
import ch.ethz.idsc.retina.dev.lidar.LidarSpacialProvider;
import ch.ethz.idsc.retina.dev.lidar.VelodyneDecoder;
import ch.ethz.idsc.retina.dev.lidar.hdl32e.Hdl32eDecoder;
import ch.ethz.idsc.retina.dev.lidar.hdl32e.Hdl32eSpacialProvider;
import ch.ethz.idsc.retina.dev.lidar.hdl32e.data.Hdl32ePanoramaCollector;
import ch.ethz.idsc.retina.dev.lidar.hdl32e.data.Hdl32ePanoramaFrame;
import ch.ethz.idsc.retina.dev.lidar.vlp16.Vlp16SpacialProvider;

public enum VelodyneUtils {
  ;
  public static Hdl32ePanoramaFrame createPanoramaDisplay(Hdl32eDecoder hdl32eRayDecoder) {
    Hdl32ePanoramaFrame hdl32ePanoramaFrame = new Hdl32ePanoramaFrame();
    Hdl32ePanoramaCollector hdl32ePanoramaCollector = new Hdl32ePanoramaCollector();
    hdl32ePanoramaCollector.addListener(hdl32ePanoramaFrame);
    hdl32eRayDecoder.addRayListener(hdl32ePanoramaCollector);
    return hdl32ePanoramaFrame;
  }

  public static VelodyneRayFrame createRayFrame( //
      LidarAngularFiringCollector lidarAngularFiringCollector, VelodyneDecoder velodyneDecoder) {
    VelodyneRayFrame velodyneFiringFrame = new VelodyneRayFrame();
    lidarAngularFiringCollector.addListener(velodyneFiringFrame);
    velodyneDecoder.addPosListener(velodyneFiringFrame);
    return velodyneFiringFrame;
  }

  public static LidarAngularFiringCollector createCollector32(VelodyneDecoder velodyneDecoder) {
    LidarAngularFiringCollector lidarAngularFiringCollector = LidarAngularFiringCollector.createDefault();
    LidarSpacialProvider lidarSpacialProvider = new Hdl32eSpacialProvider();
    lidarSpacialProvider.addListener(lidarAngularFiringCollector);
    LidarRotationProvider lidarRotationProvider = new LidarRotationProvider();
    lidarRotationProvider.addListener(lidarAngularFiringCollector);
    velodyneDecoder.addRayListener(lidarSpacialProvider);
    velodyneDecoder.addRayListener(lidarRotationProvider);
    return lidarAngularFiringCollector;
  }

  public static LidarAngularFiringCollector createCollector16(VelodyneDecoder velodyneDecoder) {
    LidarAngularFiringCollector lidarAngularFiringCollector = LidarAngularFiringCollector.createDefault();
    LidarSpacialProvider lidarSpacialProvider = new Vlp16SpacialProvider();
    lidarSpacialProvider.addListener(lidarAngularFiringCollector);
    LidarRotationProvider lidarRotationProvider = new LidarRotationProvider();
    lidarRotationProvider.addListener(lidarAngularFiringCollector);
    velodyneDecoder.addRayListener(lidarSpacialProvider);
    velodyneDecoder.addRayListener(lidarRotationProvider);
    return lidarAngularFiringCollector;
  }
}
