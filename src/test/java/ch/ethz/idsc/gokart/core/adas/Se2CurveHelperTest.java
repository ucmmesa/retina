// code by jph
package ch.ethz.idsc.gokart.core.adas;

import ch.ethz.idsc.gokart.core.OvalTrack;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class Se2CurveHelperTest extends TestCase {
  public void testClosest0() {
    int closest = Se2CurveHelper.closest(Tensors.fromString("{{1[m], 1[m], 2}, {3[m], 2[m], 4}}"), Tensors.fromString("{1.2[m], 2[m], 3}"));
    assertEquals(closest, 0);
  }

  public void testClosest1() {
    int closest = Se2CurveHelper.closest(Tensors.fromString("{{1[m], 1[m], 2}, {3[m], 2[m], 4}}"), Tensors.fromString("{2.2[m], 2[m], 3}"));
    assertEquals(closest, 1);
  }

  public void testEuclid() {
    int index = Se2CurveHelper.closestEuclid(OvalTrack.SE2);
    assertEquals(index, 179);
  }
}
