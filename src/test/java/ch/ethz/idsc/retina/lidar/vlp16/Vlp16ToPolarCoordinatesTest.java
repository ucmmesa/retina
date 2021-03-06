// code by jph
package ch.ethz.idsc.retina.lidar.vlp16;

import ch.ethz.idsc.retina.util.math.SI;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;
import ch.ethz.idsc.tensor.pdf.Distribution;
import ch.ethz.idsc.tensor.pdf.NormalDistribution;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Chop;
import junit.framework.TestCase;

public class Vlp16ToPolarCoordinatesTest extends TestCase {
  public void testUnits() {
    TensorUnaryOperator toPolar = new Vlp16ToPolarCoordinates(RealScalar.of(1.2));
    TensorUnaryOperator fromPolar = new Vlp16FromPolarCoordinates(RealScalar.of(1.2));
    Distribution distribution = NormalDistribution.standard();
    for (int count = 0; count < 100; ++count) {
      Tensor xyz = RandomVariate.of(distribution, 3).multiply(Quantity.of(1, SI.METER));
      Tensor tensor = toPolar.apply(xyz);
      Tensor inv = fromPolar.apply(tensor);
      Chop._12.requireClose(xyz, inv);
    }
  }
}
