// code by mg
package ch.ethz.idsc.gokart.gui.top;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JToggleButton;

import ch.ethz.idsc.demo.mg.pipeline.PipelineConfig;
import ch.ethz.idsc.demo.mg.util.TransformUtil;
import ch.ethz.idsc.gokart.core.pos.GokartPoseInterface;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.retina.dev.davis.DavisDevice;
import ch.ethz.idsc.retina.dev.davis._240c.Davis240c;
import ch.ethz.idsc.retina.dev.davis.app.AbstractAccumulatedImage;
import ch.ethz.idsc.retina.dev.davis.app.AccumulatedEventsGrayImage;
import ch.ethz.idsc.retina.util.TimedImageEvent;
import ch.ethz.idsc.retina.util.TimedImageListener;
import ch.ethz.idsc.retina.util.img.ImageCopy;
import ch.ethz.idsc.tensor.Tensor;

public class AccumulatedEventRender extends AbstractGokartRender implements TimedImageListener, ActionListener {
  private final DavisDevice davisDevice = Davis240c.INSTANCE;
  private final ImageCopy imageCopy;
  private final TransformUtil transformUtil;
  public final AbstractAccumulatedImage abstractAccumulatedImage = AccumulatedEventsGrayImage.of(davisDevice);
  // ..
  final JToggleButton jToggleButton = new JToggleButton("events");
  private boolean isSelected = false;
  private final double mapAheadDistance = 7; // [m]

  public AccumulatedEventRender(GokartPoseInterface gokartPoseInterface) {
    super(gokartPoseInterface);
    abstractAccumulatedImage.setInterval(25_000);
    abstractAccumulatedImage.addListener(this);
    transformUtil = new PipelineConfig().createTransformUtil();
    imageCopy = new ImageCopy();
    jToggleButton.setSelected(isSelected);
    jToggleButton.addActionListener(this);
  }

  @Override
  public void protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (!isSelected)
      return;
    // visualize events
    BufferedImage bufferedImage = imageCopy.get(); // TODO may need to make another copy
    DataBufferByte dataBufferByte = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
    byte[] bytes = dataBufferByte.getData();
    int index = 0;
    if (bytes.length == 240 * 180) {
      for (int y = 0; y < 180; y++) {
        for (int x = 0; x < 240; x++) {
          if (bytes[index] == 0 || bytes[index] == (byte) 255) {
            Tensor mappedEvent = transformUtil.imageToWorldTensor(x, y);
            if (mappedEvent.Get(0).number().doubleValue() < mapAheadDistance) {
              Point2D point = geometricLayer.toPoint2D(mappedEvent);
              Color eventColor = (bytes[index] == 0) ? Color.GREEN : Color.RED;
              graphics.setColor(eventColor);
              graphics.fillRect((int) point.getX(), (int) point.getY(), 1, 1);
            }
          }
          ++index;
        }
      }
    }
  }

  @Override
  public void timedImage(TimedImageEvent timedImageEvent) {
    imageCopy.update(timedImageEvent.bufferedImage);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    isSelected = jToggleButton.isSelected();
  }
}
