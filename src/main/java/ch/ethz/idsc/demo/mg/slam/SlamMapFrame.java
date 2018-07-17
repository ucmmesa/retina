// code by mg
package ch.ethz.idsc.demo.mg.slam;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;
import java.util.stream.IntStream;

import org.bytedeco.javacpp.opencv_core.Mat;

import ch.ethz.idsc.demo.mg.util.CVUtil;
import ch.ethz.idsc.demo.mg.util.VisualizationUtil;
import ch.ethz.idsc.tensor.Tensor;

/** gives an image of the maps generated by the SLAM algorithm */
class SlamMapFrame {
  private static final byte CLEAR_BYTE = (byte) 255;
  private static final byte ORANGE = (byte) -52;
  private static final byte GREEN = (byte) 30;
  private static final byte BLUE = (byte) 5;
  private static final byte RED = (byte) -76;
  private final BufferedImage bufferedImage;
  private final Graphics2D graphics;
  private final double[] mapArray;
  private final byte[] bytes;
  private final double cornerX;
  private final double cornerY;
  private final double cellDim;
  private final int kartLength;
  private final int frameWidth;
  private final int frameHeight;
  private final int numberOfCells;
  private double maxValue;

  SlamMapFrame(SlamConfig slamConfig) {
    frameWidth = slamConfig.dimX.divide(slamConfig.cellDim).number().intValue();
    frameHeight = slamConfig.dimY.divide(slamConfig.cellDim).number().intValue();
    numberOfCells = frameWidth * frameHeight;
    cornerX = slamConfig.corner.Get(0).number().doubleValue();
    cornerY = slamConfig.corner.Get(1).number().doubleValue();
    cellDim = slamConfig.cellDim.number().doubleValue();
    kartLength = (int) (slamConfig.kartSize.number().doubleValue() / cellDim);
    mapArray = new double[numberOfCells];
    bufferedImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_BYTE_INDEXED);
    graphics = bufferedImage.createGraphics();
    DataBufferByte dataBufferByte = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
    bytes = dataBufferByte.getData();
  }

  public void setRawMap(MapProvider map) {
    // safety check
    if (map.getNumberOfCells() != numberOfCells)
      System.out.println("Fatal: something went wrong!");
    else {
      double[] newMapArray = map.getMapArray();
      for (int i = 0; i < newMapArray.length; i++) {
        mapArray[i] = newMapArray[i];
      }
      maxValue = map.getMaxValue();
      paintRawMap();
    }
  }

  // draws the frame according to the mapArray values. values are normalized by maxValue
  private void paintRawMap() {
    if (maxValue == 0)
      clearFrame();
    else {
      for (int i = 0; i < bytes.length; i++) {
        bytes[i] = (byte) (216 + 39 * (1 - mapArray[i] / maxValue));
      }
    }
  }

  public void addGokartPose(Tensor pose, Color color) {
    double posX = pose.Get(0).number().doubleValue();
    double posY = pose.Get(1).number().doubleValue();
    double rotAngle = pose.Get(2).number().doubleValue();
    int pixelPoseX = (int) ((posX - cornerX) / cellDim);
    int pixelPoseY = (int) ((posY - cornerY) / cellDim);
    Ellipse2D ellipse = new Ellipse2D.Float(pixelPoseX - kartLength / 2, pixelPoseY - kartLength / 4, kartLength, kartLength / 2);
    AffineTransform old = graphics.getTransform();
    graphics.rotate(rotAngle, pixelPoseX, pixelPoseY);
    graphics.setColor(color);
    graphics.draw(ellipse);
    graphics.fill(ellipse);
    graphics.setTransform(old);
  }

  // return flipped image such that x axis points right and y axis upwards
  public BufferedImage getFrame() {
    return VisualizationUtil.flipHorizontal(bufferedImage);
  }

  public void setWayPoints(List<WayPoint> wayPoints) {
    clearFrame();
    int width = 20;
    int height = 20;
    for (int i = 0; i < wayPoints.size(); i++) {
      double[] framePos = worldToFrame(wayPoints.get(i).getWorldPosition());
      Ellipse2D ellipse = new Ellipse2D.Double(framePos[0] - width / 2, framePos[1] - height / 2, width, height);
      // paint waypoints according to visibility
      if (wayPoints.get(i).getVisibility()) {
        graphics.setColor(Color.GREEN);
      } else {
        graphics.setColor(Color.ORANGE);
      }
      graphics.fill(ellipse);
    }
  }

  private void clearFrame() {
    IntStream.range(0, bytes.length).forEach(i -> bytes[i] = CLEAR_BYTE);
  }

  // TODO maybe move to static utility class
  private double[] worldToFrame(double[] worldPos) {
    double[] framePos = new double[2];
    framePos[0] = (worldPos[0] - cornerX) / cellDim;
    framePos[1] = (worldPos[1] - cornerY) / cellDim;
    return framePos;
  }

  public void setProcessedMat(Mat processedMat) {
    byte[] processedByteArray = CVUtil.matToByteArray(processedMat);
    for (int i = 0; i < bytes.length; i++) {
      // experimental code below
      if (processedByteArray[i] == 0) {
        bytes[i] = CLEAR_BYTE;
      } else {
        int labelID = processedByteArray[i] % 3;
        switch (labelID) {
        case 0:
          bytes[i] = ORANGE;
          break;
        case 1:
          bytes[i] = GREEN;
          break;
        case 2:
          bytes[i] = BLUE;
          break;
        }
      }
    }
  }
}
