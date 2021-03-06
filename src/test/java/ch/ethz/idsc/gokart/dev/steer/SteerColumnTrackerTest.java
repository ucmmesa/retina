// code by jph
package ch.ethz.idsc.gokart.dev.steer;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Clips;
import junit.framework.TestCase;

public class SteerColumnTrackerTest extends TestCase {
  public void testSimple() {
    SteerColumnTracker steerColumnTracker = new SteerColumnTracker();
    assertFalse(steerColumnTracker.isSteerColumnCalibrated());
    assertFalse(steerColumnTracker.isCalibratedAndHealthy());
  }

  public void testSimpleFail() {
    try {
      new SteerColumnTracker().getSteerColumnEncoderCentered();
      fail();
    } catch (Exception exception) {
      // ---
    }
  }

  public void testMaxRange() {
    Clip clip = Clips.interval(Quantity.of(0.7, "SCE"), Quantity.of(0.73, "SCE"));
    clip.requireInside(SteerConfig.GLOBAL.columnMax);
  }

  public void testFeed() {
    SteerColumnTracker steerColumnTracker = new SteerColumnTracker();
    assertFalse(steerColumnTracker.isSteerColumnCalibrated());
    steerColumnTracker.getEvent(SteerGetFactory.create(+0.75f));
    assertFalse(steerColumnTracker.isSteerColumnCalibrated());
    steerColumnTracker.getEvent(SteerGetFactory.create(-0.75f));
    assertTrue(steerColumnTracker.isSteerColumnCalibrated());
    assertTrue(steerColumnTracker.isCalibratedAndHealthy());
    Scalar scalar = steerColumnTracker.getSteerColumnEncoderCentered();
    assertTrue(scalar instanceof Quantity);
    steerColumnTracker.getEvent(SteerGetFactory.create(+0.79f)); // 1.54
    assertTrue(Math.abs(steerColumnTracker.getIntervalWidth() - 1.5400000214576721) < 1e-6);
    assertTrue(steerColumnTracker.isSteerColumnCalibrated());
    assertTrue(steerColumnTracker.isCalibratedAndHealthy());
    steerColumnTracker.getEvent(SteerGetFactory.create(-0.76f)); // >1.55
    assertTrue(steerColumnTracker.isCalibratedAndHealthy());
    steerColumnTracker.getEvent(SteerGetFactory.create(-0.86f)); // >1.65
    assertFalse(steerColumnTracker.isCalibratedAndHealthy());
  }

  public void testFeedRelRckQual() {
    SteerColumnTracker steerColumnTracker = new SteerColumnTracker();
    assertFalse(steerColumnTracker.isSteerColumnCalibrated());
    steerColumnTracker.getEvent(SteerGetFactory.create(+0.75f));
    assertFalse(steerColumnTracker.isSteerColumnCalibrated());
    steerColumnTracker.getEvent(SteerGetFactory.create(-0.75f));
    assertTrue(steerColumnTracker.isSteerColumnCalibrated());
    assertTrue(steerColumnTracker.isCalibratedAndHealthy());
    steerColumnTracker.getEvent(SteerGetFactory.create(+0.79f, SteerGetStatus.DISABLED.value())); // 1.54
    assertFalse(steerColumnTracker.isSteerColumnCalibrated());
    assertFalse(steerColumnTracker.isCalibratedAndHealthy());
  }
}
