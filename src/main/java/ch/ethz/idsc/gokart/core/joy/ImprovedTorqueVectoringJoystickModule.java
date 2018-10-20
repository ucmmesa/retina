// code by mh
package ch.ethz.idsc.gokart.core.joy;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Sign;

public class ImprovedTorqueVectoringJoystickModule extends SimpleTorqueVectoring {
  public ImprovedTorqueVectoringJoystickModule(TorqueVectoringConfig torqueVectoringConfig) {
    super(torqueVectoringConfig);
  }

  @Override // from SimpleTorqueVectoring
  Scalar wantedZTorque(Scalar wantedZTorque, Scalar realRotation) {
    if (Sign.isNegative(realRotation.multiply(wantedZTorque))) {
      Scalar scalar = Clip.unit().apply(realRotation.abs().multiply(torqueVectoringConfig.ks));
      Scalar stabilizerFactor = RealScalar.ONE.subtract(scalar);
      wantedZTorque = wantedZTorque.multiply(stabilizerFactor);
    }
    return wantedZTorque;
  }
}
