// code by mg
package ch.ethz.idsc.demo.mg.slam.algo.prc;

import ch.ethz.idsc.owl.math.map.Se2CoveringExponential;
import ch.ethz.idsc.owl.math.map.Se2CoveringGroupAction;
import ch.ethz.idsc.owl.math.planar.SignedCurvature2D;
import ch.ethz.idsc.retina.dev.steer.SteerConfig;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.sca.ArcTan;

/** methods for curve extrapolation in SLAM algorithm */
/* package */ enum SlamCurveExtrapolate {
  ;
  // constant is equal to maximum path curvature that the vehicle can drive
  private static final Scalar MAX_PATH_CURVATURE = RealScalar.of(SteerConfig.GLOBAL.turningRatioMax.number().doubleValue());

  /** extrapolates last point of curve with a circle segment of curvature multiplied with curveFactor
   * 
   * @param curve
   * @param localCurvature curvature at the second last point of curve
   * @param curveFactor factor with which localcurvature is multiplied
   * @param distance length of extrapolated circle segment
   * @param numberOfPoints number of points along extrapolated segment
   * @return extrapolatedCurve */
  public static Tensor extrapolateCurve(Tensor curve, Scalar localCurvature, Scalar curveFactor, Scalar distance, Scalar numberOfPoints) {
    Tensor endPose = getEndPose(curve);
    Tensor extrapolatedCurve = Tensors.of(endPose.extract(0, 2));
    // negate to extrapolate to inner side of curve
    localCurvature = localCurvature.multiply(curveFactor).negate();
    localCurvature = limitCurvature(localCurvature);
    Tensor circleParam = Tensors.vector(1, 0, localCurvature.number().doubleValue());
    Se2CoveringGroupAction se2CoveringGroupAction = new Se2CoveringGroupAction(endPose);
    Scalar stepSize = distance.divide(numberOfPoints);
    for (int i = 0; i < numberOfPoints.number().intValue(); ++i) {
      Tensor extrapolatedPoint = se2CoveringGroupAction.combine(Se2CoveringExponential.INSTANCE.exp(circleParam.multiply(stepSize.multiply(RealScalar.of(i)))));
      extrapolatedPoint = extrapolatedPoint.extract(0, 2);
      extrapolatedCurve.append(extrapolatedPoint);
    }
    return extrapolatedCurve;
  }

  /** @param curve
   * @return pose of last point of curve looking in tangent direction */
  private static Tensor getEndPose(Tensor curve) {
    Tensor endHeading = getEndHeading(curve);
    Tensor endPose = curve.get(curve.length() - 1).append(endHeading);
    return endPose;
  }

  public static Scalar getEndHeading(Tensor curve) {
    Tensor direction = curve.get(curve.length() - 1).subtract(curve.get(curve.length() - 2));
    return ArcTan.of(direction.Get(0), direction.Get(1));
  }

  private static Scalar limitCurvature(Scalar curvature) {
    if (Scalars.lessEquals(curvature, MAX_PATH_CURVATURE.negate()))
      return MAX_PATH_CURVATURE.negate();
    if (Scalars.lessEquals(MAX_PATH_CURVATURE, curvature))
      return MAX_PATH_CURVATURE;
    return curvature;
  }

  /** @param curve
   * @return curvature of second last point of curve, clipped by MAX_PATH_CURVATURE */
  public static Scalar getLocalCurvature(Tensor curve) {
    int curvaturePoint = curve.length() - 2;
    Tensor prev = curve.get(curvaturePoint - 1);
    Tensor current = curve.get(curvaturePoint);
    Tensor next = curve.get(curvaturePoint + 1);
    Scalar localCurvature = SignedCurvature2D.of(prev, current, next).get();
    return limitCurvature(localCurvature);
  }
}
