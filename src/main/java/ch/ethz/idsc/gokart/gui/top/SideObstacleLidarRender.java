// code by vc
package ch.ethz.idsc.gokart.gui.top;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Objects;

import ch.ethz.idsc.gokart.calib.SensorsConfig;
import ch.ethz.idsc.gokart.core.fuse.SafetyConfig;
import ch.ethz.idsc.gokart.core.perc.SpacialXZObstaclePredicate;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.sophus.lie.se2.Se2Matrix;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

class SideObstacleLidarRender extends LidarRender {
  private final SpacialXZObstaclePredicate spacialXZObstaclePredicate = SafetyConfig.GLOBAL.createSpacialXZObstaclePredicate();

  @Override // from AbstractGokartRender
  public void protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    geometricLayer.pushMatrix(Se2Matrix.of(supplier.get()));
    Tensor translate = SensorsConfig.GLOBAL.vlp16_sideMatrix();
    geometricLayer.pushMatrix(translate);
    {
      Point2D point2D = geometricLayer.toPoint2D(Tensors.vector(0, 0));
      Point2D width = geometricLayer.toPoint2D(Tensors.vector(0.1, 0));
      double w = point2D.distance(width);
      graphics.setColor(new Color(0, 128, 0, 128));
      graphics.fill(new Ellipse2D.Double(point2D.getX() - w / 2, point2D.getY() - w / 2, w, w));
    }
    if (Objects.nonNull(_points)) {
      Tensor points = _points;
      graphics.setColor(color);
      for (Tensor point : points) {
        if (spacialXZObstaclePredicate.isObstacle(point)) {
          double x = point.Get(0).number().doubleValue();
          double z = point.Get(2).number().doubleValue();
          Point2D point2D = geometricLayer.toPoint2D(x, z);
          graphics.fillRect((int) point2D.getX(), (int) point2D.getY(), pointSize, pointSize);
        }
      }
    }
    geometricLayer.popMatrix();
    geometricLayer.popMatrix();
  }
}
