// code by jph
package ch.ethz.idsc.gokart.calib.steer;

import ch.ethz.idsc.gokart.dev.steer.SteerColumnInterface;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Series;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

/** gives the angle of the front wheel (left or right) depending on steer column encoder value
 * 
 * the numeric approximation of the mapping SCE -> wheel angle with interpretation in radian
 * is based on report
 * https://github.com/idsc-frazzoli/retina/files/2440459/20181001_steering_measurement.pdf
 * 
 * the mapping is not bijective, therefore the inverse mapping is not supported */
public enum FrontWheelSteerMapping implements SteerMapping {
  _LEFT(RealScalar.of(0.0), Quantity.of(0.939002, "SCE^-1"), Quantity.of(+0.302693, "SCE^-2"), Quantity.of(-0.624484, "SCE^-3")), //
  RIGHT(RealScalar.of(0.0), Quantity.of(0.939002, "SCE^-1"), Quantity.of(-0.302693, "SCE^-2"), Quantity.of(-0.624484, "SCE^-3")), //
  ;
  private final ScalarUnaryOperator scalarUnaryOperator;

  private FrontWheelSteerMapping(Scalar... scalars) {
    scalarUnaryOperator = Series.of(Tensors.of(scalars));
  }

  @Override // from SteerMapping
  public Scalar getRatioFromSCE(SteerColumnInterface steerColumnInterface) {
    return getRatioFromSCE(steerColumnInterface.getSteerColumnEncoderCentered());
  }

  @Override // from SteerMapping
  public Scalar getRatioFromSCE(Scalar scalar) {
    return scalarUnaryOperator.apply(scalar);
  }

  @Override // from SteerMapping
  public Scalar getSCEfromRatio(Scalar angle) {
    throw new UnsupportedOperationException();
  }
}
