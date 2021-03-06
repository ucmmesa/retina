// code by gjoel
package ch.ethz.idsc.gokart.lcm.led;

import java.awt.Color;
import java.nio.ByteBuffer;

import ch.ethz.idsc.gokart.dev.led.LEDStatus;
import ch.ethz.idsc.gokart.lcm.ArrayFloatBlob;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.VectorQ;
import idsc.BinaryBlob;
import lcm.lcm.LCM;

public class LEDLcm {
  /** encode uses the method ArrayFloatBlob::encode to turn the received message
   * (in this case, a {@link LEDStatus}) into a BinaryBlob */
  public static BinaryBlob encode(LEDStatus ledStatus) {
    return ArrayFloatBlob.encode(ledStatus.asVector());
  }

  /** publish sends the BinaryBlob through the "led.color" channel
   * using LCM.getSingleton()::publish. */
  public static void publish(String channel, LEDStatus ledStatus) {
    LCM.getSingleton().publish(channel, encode(ledStatus));
  }

  /** decode gets back {@link LEDStatus} from a ByteBuffer using ArrayFloatBlob::decode. */
  public static LEDStatus decode(ByteBuffer byteBuffer) {
    Tensor decoded = VectorQ.requireLength(ArrayFloatBlob.decode(byteBuffer), LEDStatus.LENGTH);
    int[] array = decoded.stream().mapToInt(t -> t.Get().number().intValue()).toArray();
    return new LEDStatus(array[0], array[1], new Color(array[2], array[3], array[4]));
  }
}
