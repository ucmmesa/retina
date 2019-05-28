// code by mh, jph
package ch.ethz.idsc.owl.car.math;

import ch.ethz.idsc.tensor.Scalar;

public class AngularSlip {
  /** tangentSpeed with unit m*s^-1 */
  private final Scalar tangentSpeed;
  /** rotationPerMeterDriven with unit m^-1 */
  private final Scalar rotationPerMeterDriven;
  /** wantedRotationRate with unit s^-1 */
  private final Scalar wantedRotationRate;
  /** gyroZ with unit s^-1 */
  private final Scalar gyroZ;

  /** @param tangentSpeed m*s^-1
   * @param rotationPerMeterDriven m^-1
   * @param gyroZ s^-1 */
  public AngularSlip(Scalar tangentSpeed, Scalar rotationPerMeterDriven, Scalar gyroZ) {
    this.tangentSpeed = tangentSpeed;
    this.rotationPerMeterDriven = rotationPerMeterDriven;
    wantedRotationRate = rotationPerMeterDriven.multiply(tangentSpeed); // unit s^-1
    this.gyroZ = gyroZ;
  }

  /** @return tangentSpeed with unit m*s^-1 */
  public Scalar tangentSpeed() {
    return tangentSpeed;
  }

  /** @return rotationPerMeterDriven with unit m^-1 */
  public Scalar rotationPerMeterDriven() {
    return rotationPerMeterDriven;
  }

  /** @return wantedRotationRate with unit s^-1 */
  public Scalar wantedRotationRate() {
    return wantedRotationRate;
  }

  /** @return gyroZ with unit s^-1 */
  public Scalar gyroZ() {
    return gyroZ;
  }

  /** @return difference between wantedRotationRate and gyroZ */
  public Scalar angularSlip() {
    return wantedRotationRate.subtract(gyroZ);
  }
}
