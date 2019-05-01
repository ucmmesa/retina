// code by jph
package ch.ethz.idsc.owl.bot.se2.pid;

import java.io.IOException;

import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.retina.util.math.SI;
import ch.ethz.idsc.sophus.group.Se2CoveringIntegrator;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.io.TableBuilder;
import ch.ethz.idsc.tensor.opt.Pi;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Clips;
import junit.framework.TestCase;

public class ConvergenceTest extends TestCase {
  /** A = Import["posepid.csv"];
   * ListPlot[A[[All, {1, 2}]], AspectRatio -> 1, PlotRange -> All] */
  public void testSimple() throws IOException {
    Scalar maxTurningRate = Quantity.of(0.3, SI.PER_SECOND);
    Clip turningRate = Clips.interval(maxTurningRate.negate(), maxTurningRate);
    PIDGains pidGains = new PIDGains(Quantity.of(.4, "m^-1"), RealScalar.ZERO, Quantity.of(3, "s*m^-1"));
    PIDTrajectory pidTrajectory = null;
    // for (many different initial pose)
    TableBuilder tableBuilder = new TableBuilder();
    Tensor pose = Tensors.fromString("{0[m],2[m],0}");
    Tensor traj = Tensors.vector(i -> Tensors.of(Quantity.of(i / 10, SI.METER), Quantity.of(1, SI.METER), Pi.HALF), 2000);
    for (int index = 0; index < 100; ++index) {
      StateTime stateTime = new StateTime(pose, Quantity.of(index, SI.SECOND));
      PIDTrajectory _pidTrajectory = new PIDTrajectory(index, pidTrajectory, pidGains, traj, stateTime);
      pidTrajectory = _pidTrajectory;
      Scalar angleOut = pidTrajectory.angleOut();
      System.out.println("angleOut=" + angleOut);
      if (true)
        break;
      // clip within valid angle [-max, max]
      // FIXME MCP angleOut should have different unit
      angleOut = turningRate.apply(angleOut);
      double dt = 0.1;
      Tensor vel = Tensors.of(Quantity.of(2, SI.VELOCITY), Quantity.of(0, SI.VELOCITY), angleOut); // this is correct
      // System.out.println("vel="+vel);
      pose = Se2CoveringIntegrator.INSTANCE. // Euler
          spin(pose, vel.multiply(Quantity.of(dt, SI.SECOND)));
      stateTime = new StateTime(pose, stateTime.time().add(Quantity.of(dt, SI.SECOND)));
      System.out.println(pose);
      tableBuilder.appendRow(pose);
      // System.out.println("angle out " + angleOut);
      // System.out.println(pidTrajectory.getProp());
      // System.out.println(pidTrajectory.getDeriv());
      // System.out.println("------------------_");
    }
    Export.of(HomeDirectory.file("posepid.csv"), tableBuilder.toTable());
  }
}
