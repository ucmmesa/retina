// code by jph
package ch.ethz.idsc.gokart.offline.channel;

import java.nio.ByteBuffer;

import ch.ethz.idsc.gokart.calib.SensorsConfig;
import ch.ethz.idsc.gokart.calib.vmu931.PlanarVmu931Imu;
import ch.ethz.idsc.retina.imu.vmu931.Vmu931ImuFrame;
import ch.ethz.idsc.retina.util.math.Magnitude;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.sca.Round;

public enum Vmu931ImuVehicleChannel implements SingleChannelInterface {
  INSTANCE;
  // ---
  private final PlanarVmu931Imu planarVmu931Imu = SensorsConfig.GLOBAL.getPlanarVmu931Imu();

  @Override // from SingleChannelInterface
  public String channel() {
    return Vmu931ImuChannel.INSTANCE.channel();
  }

  @Override // from SingleChannelInterface
  public String exportName() {
    return channel() + ".vehicle";
  }

  @Override // from SingleChannelInterface
  public Tensor row(ByteBuffer byteBuffer) {
    Vmu931ImuFrame vmu931ImuFrame = new Vmu931ImuFrame(byteBuffer);
    return Tensors.of( //
        RealScalar.of(vmu931ImuFrame.timestamp_ms()), //
        planarVmu931Imu.accXY(vmu931ImuFrame).map(Magnitude.ACCELERATION).map(Round._8), //
        planarVmu931Imu.gyroZ(vmu931ImuFrame).map(Magnitude.PER_SECOND).map(Round._8));
  }
}
