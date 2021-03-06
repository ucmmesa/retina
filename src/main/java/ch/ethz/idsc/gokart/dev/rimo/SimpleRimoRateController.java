// concept by jelavice
// code by jph
package ch.ethz.idsc.gokart.dev.rimo;

import ch.ethz.idsc.retina.util.math.NonSI;
import ch.ethz.idsc.retina.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/** two instance of this class are used for left and right rear motors
 * @see RimoRateControllerDuo
 * 
 * Kp with unit "ARMS*rad^-1*s"
 * Ki with unit "ARMS*rad^-1" */
/* package */ class SimpleRimoRateController implements RimoRateController {
  static final Scalar DT = RimoSocket.INSTANCE.getPutPeriod();
  // ---
  private final RimoConfig rimoConfig;
  // ---
  /** pos error initially incorrect in the first iteration */
  private Scalar lastVel_error = Quantity.of(0, SI.PER_SECOND); // unit "s^-1"
  private Scalar lastTor_value = Quantity.of(0, NonSI.ARMS); // unit "ARMS"

  public SimpleRimoRateController(RimoConfig rimoConfig) {
    this.rimoConfig = rimoConfig;
  }

  @Override // from RimoRateController
  public Scalar iterate(final Scalar vel_error) {
    final Scalar pPart = vel_error.subtract(lastVel_error).multiply(rimoConfig.Kp);
    final Scalar iPart = vel_error.multiply(rimoConfig.Ki).multiply(DT);
    lastVel_error = vel_error;
    final Scalar tor_value = lastTor_value.add(pPart).add(iPart);
    lastTor_value = rimoConfig.torqueLimitClip().apply(tor_value); // anti-windup
    return lastTor_value;
  }

  @Override
  public void setWheelRate(Scalar vel_avg) {
    // ---
  }
}
