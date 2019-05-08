// code by jph
package ch.ethz.idsc.gokart.calib.steer;

import java.util.Arrays;
import java.util.List;

import ch.ethz.idsc.gokart.dev.steer.SteerColumnAdapter;
import ch.ethz.idsc.gokart.dev.steer.SteerConfig;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.qty.QuantityUnit;
import ch.ethz.idsc.tensor.qty.Unit;
import ch.ethz.idsc.tensor.sca.Clips;
import ch.ethz.idsc.tensor.sca.Sign;
import junit.framework.TestCase;

public class SteerMappingTest extends TestCase {
  private static final List<SteerMapping> STEER_MAPPINGS = Arrays.asList( //
      LinearSteerMapping.INSTANCE, //
      CubicSteerMapping.instance(), //
      FittedSteerMapping.instance());

  public void testAdvancedFormulaCenter() {
    for (SteerMapping steerMapping : STEER_MAPPINGS) {
      Scalar angle = steerMapping.getAngleFromSCE( //
          new SteerColumnAdapter(true, Quantity.of(0, "SCE")));
      assertEquals(angle, RealScalar.ZERO);
      Scalar sce = steerMapping.getSCEfromAngle(angle);
      assertTrue(Scalars.isZero(sce));
    }
  }

  public void testAdvancedFormulaSign() {
    for (SteerMapping steerMapping : STEER_MAPPINGS) {
      Scalar sceIn = Quantity.of(0.1, "SCE");
      Scalar angle = steerMapping.getAngleFromSCE( //
          new SteerColumnAdapter(true, sceIn));
      assertTrue(Sign.isPositive(angle));
      Clips.interval(.05, .1).requireInside(angle);
      Scalar sce = steerMapping.getSCEfromAngle(angle);
      assertTrue(Scalars.lessThan(sce.subtract(sceIn).abs(), Quantity.of(0.01, "SCE")));
    }
  }

  public void testAdvancedFormulaNegative() {
    for (SteerMapping steerMapping : STEER_MAPPINGS) {
      Scalar sceIn = Quantity.of(-0.7, "SCE");
      Scalar angle = steerMapping.getAngleFromSCE( //
          new SteerColumnAdapter(true, sceIn));
      assertTrue(Sign.isNegative(angle));
      Clips.interval(-.5, -.4).requireInside(angle);
      Scalar sce = steerMapping.getSCEfromAngle(angle);
      assertTrue(Scalars.lessThan(sce.subtract(sceIn).abs(), Quantity.of(0.05, "SCE")));
    }
  }

  public void testExtremePos() {
    for (SteerMapping steerMapping : STEER_MAPPINGS) {
      Scalar q = steerMapping.getSCEfromAngle(Quantity.of(+0.45, ""));
      assertEquals(QuantityUnit.of(q), Unit.of("SCE"));
      assertTrue(0.64 < q.number().doubleValue());
    }
  }

  public void testExtremeNeg() {
    for (SteerMapping steerMapping : STEER_MAPPINGS) {
      Scalar q = steerMapping.getSCEfromAngle(Quantity.of(-0.45, ""));
      assertEquals(QuantityUnit.of(q), Unit.of("SCE"));
      assertTrue(q.number().doubleValue() < -0.64);
    }
  }

  public void testContainment() {
    assertTrue(STEER_MAPPINGS.contains(SteerConfig.GLOBAL.getSteerMapping()));
  }
}
