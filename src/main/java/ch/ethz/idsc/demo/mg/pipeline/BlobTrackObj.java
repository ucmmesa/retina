// code by mg
package ch.ethz.idsc.demo.mg.pipeline;

import ch.ethz.idsc.retina.dev.davis._240c.DavisDvsEvent;

// provides blob object for the tracking algorithm.
public class BlobTrackObj {
  // camera parameters
  private static int width;
  private static int height;
  // blob parameters
  private final float[] initPos;
  private final float[] pos;
  private final double[][] covariance;
  private float activity;
  private boolean layerID; // true for active layer, false for hidden layer
  private float currentScore;

  // initialize with position and covariance
  BlobTrackObj(float initialX, float initialY, float initVariance) {
    initPos = new float[] { initialX, initialY };
    pos = new float[] { initialX, initialY };
    covariance = new double[][] { { initVariance, 0 }, { 0, initVariance } };
    layerID = false;
    activity = 0.0f;
    currentScore = 0.0f;
  }

  // set static parameters of class
  public static void setParams(PipelineConfig pipelineConfig) {
    width = pipelineConfig.width.number().intValue();
    height = pipelineConfig.height.number().intValue();
  }

  // updates the activity of a blob
  public boolean updateBlobActivity(boolean hasHighestScore, float aUp, float exponential) {
    boolean isPromoted;
    if (hasHighestScore) {
      // if hidden layer blob hits threshold it should be promoted
      float potentialActivity = activity * exponential + currentScore;
      isPromoted = !layerID && potentialActivity > aUp;
      activity = potentialActivity;
      return isPromoted;
    }
    activity = activity * exponential;
    isPromoted = false;
    return isPromoted;
  }

  // updates parameters of matching blob
  public void updateBlobParameters(DavisDvsEvent davisDvsEvent, float alphaOne, float alphaTwo) {
    float eventPosX = davisDvsEvent.x;
    float eventPosY = davisDvsEvent.y;
    // position update
    pos[0] = alphaOne * pos[0] + (1 - alphaOne) * eventPosX;
    pos[1] = alphaOne * pos[1] + (1 - alphaOne) * eventPosY;
    // delta covariance
    float deltaXX = (eventPosX - pos[0]) * (eventPosX - pos[0]);
    float deltaXY = (eventPosX - pos[0]) * (eventPosY - pos[1]);
    float deltaYY = (eventPosY - pos[1]) * (eventPosY - pos[1]);
    float[][] deltaCovariance = { { deltaXX, deltaXY }, { deltaXY, deltaYY } };
    // covariance update
    covariance[0][0] = alphaTwo * covariance[0][0] + (1 - alphaTwo) * deltaCovariance[0][0];
    covariance[0][1] = alphaTwo * covariance[0][1] + (1 - alphaTwo) * deltaCovariance[0][1];
    covariance[1][0] = alphaTwo * covariance[1][0] + (1 - alphaTwo) * deltaCovariance[1][0];
    covariance[1][1] = alphaTwo * covariance[1][1] + (1 - alphaTwo) * deltaCovariance[1][1];
  }

  // scoring function based on Gaussian distribution
  public float gaussianBlobScore(DavisDvsEvent davisDvsEvent) {
    float eventPosX = davisDvsEvent.x;
    float eventPosY = davisDvsEvent.y;
    // determinant and inverse
    double covarianceDeterminant = covariance[0][0] * covariance[1][1] - covariance[0][1] * covariance[1][0];
    double[][] covarianceInverse = { { covariance[1][1], -covariance[0][1] }, { -covariance[1][0], covariance[0][0] } };
    covarianceInverse[0][0] /= covarianceDeterminant;
    covarianceInverse[0][1] /= covarianceDeterminant;
    covarianceInverse[1][0] /= covarianceDeterminant;
    covarianceInverse[1][1] /= covarianceDeterminant;
    // compute exponent of Gaussian distribution
    float offsetX = eventPosX - pos[0];
    float offsetY = eventPosY - pos[1];
    double[] intermediate = { covarianceInverse[0][0] * offsetX + covarianceInverse[0][1] * offsetY,
        covarianceInverse[1][0] * offsetX + covarianceInverse[1][1] * offsetY };
    float exponent = (float) (-0.5 * (offsetX * intermediate[0] + offsetY * intermediate[1]));
    currentScore = (float) (1 / (2 * Math.PI) * 1 / Math.sqrt(covarianceDeterminant) * Math.exp(exponent));
    return currentScore;
  }

  // scoring function based on Gabor filters
  // TODO how to incorporate event polarity?
  public float gaborBlobScore(DavisDvsEvent davisDvsEvent) {
    double sigma = 3;
    double gamma = sigma / 15;
    double lambda = 4 * sigma;
    double theta = Math.PI / 2;
    double xU = (davisDvsEvent.x - pos[0]) * Math.cos(theta) + (davisDvsEvent.y - pos[1]) * Math.sin(theta);
    double yU = -(davisDvsEvent.x - pos[0]) * Math.sin(theta) + (davisDvsEvent.y - pos[1]) * Math.cos(theta);
    currentScore = (float) Math.exp((xU * xU + gamma * gamma * yU * yU) / (2 * sigma * sigma) * Math.cos(2 * Math.PI * xU / lambda));
    return currentScore;
  }

  // maybe use also Manhattan distance?
  public float geometricBlobScore(DavisDvsEvent davisDvsEvent) {
    double distance = Math.sqrt((davisDvsEvent.x - pos[0]) * (davisDvsEvent.x - pos[0]) + (davisDvsEvent.y - pos[1]) * (davisDvsEvent.y - pos[1]));
    // somehow normalize the distance
    return (float) distance;
  }

  public void updateAttractionEquation(float alphaAttr, float dRep) {
    float posDiff = (float) Math.sqrt((pos[0] - initPos[0]) * (pos[0] - initPos[0]) + (pos[1] - initPos[1]) * (pos[1] - initPos[1]));
    if (posDiff > dRep) {
      pos[0] = initPos[0];
      pos[1] = initPos[1];
    } else {
      pos[0] = pos[0] + alphaAttr * (initPos[0] - pos[0]);
      pos[1] = pos[1] + alphaAttr * (initPos[1] - pos[1]);
    }
  }

  // required for merging
  public float getDistanceTo(BlobTrackObj otherBlob) {
    double distance = Math
        .sqrt((pos[0] - otherBlob.getPos()[0]) * (pos[0] - otherBlob.getPos()[0]) + (pos[1] - otherBlob.getPos()[1]) * (pos[1] - otherBlob.getPos()[1]));
    return (float) distance;
  }

  // merge blobs by using activity-weighted average
  public void eat(BlobTrackObj otherBlob) {
    float totActivity = activity + otherBlob.getActivity();
    // position merge
    pos[0] = (1 / totActivity) * (activity * pos[0] + otherBlob.getActivity() * otherBlob.getPos()[0]);
    pos[1] = (1 / totActivity) * (activity * pos[1] + otherBlob.getActivity() * otherBlob.getPos()[1]);
    // covariance merge TODO find out which is the correct way to do that
    covariance[0][0] = 0.5 * (covariance[0][0] + otherBlob.getCovariance()[0][0]);
    covariance[0][1] = 0.5 * (covariance[0][1] + otherBlob.getCovariance()[0][1]);
    covariance[1][0] = 0.5 * (covariance[1][0] + otherBlob.getCovariance()[1][0]);
    covariance[1][1] = 0.5 * (covariance[1][1] + otherBlob.getCovariance()[1][1]);
    // acitivty merge... TODO is it reasonable?
    activity = totActivity;
  }

  public boolean blobPromotion(float aUp) {
    layerID = activity > aUp;
    return layerID;
  }

  // if blob is too close to boundary, return true
  public boolean isOutOfBounds(int boundaryDistance) {
    float boundPointLeft = pos[0] - boundaryDistance;
    float boundPointRight = pos[0] + boundaryDistance;
    float boundPointUp = pos[1] - boundaryDistance;
    float boundPointDown = pos[1] + boundaryDistance;
    return boundPointLeft < 0 || boundPointRight > (width - 1) || boundPointUp < 0 || boundPointDown > height;
  }

  public void increaseBlobSize(float enlargement) {
    covariance[0][0] *= enlargement;
    covariance[0][1] *= enlargement;
    covariance[1][0] *= enlargement;
    covariance[1][1] *= enlargement;
  }

  // return a size metric, currently trace of matrix
  public double getSizeMetric() {
    return covariance[0][0] + covariance[1][1];
  }

  public void setLayerID(boolean layerID) {
    this.layerID = layerID;
  }

  public boolean getLayerID() {
    return layerID;
  }

  public float getActivity() {
    return activity;
  }

  public float[] getPos() {
    return pos;
  }

  public float[] getInitPos() {
    return initPos;
  }

  public double[][] getCovariance() {
    return covariance;
  }

  public float getScore() {
    return currentScore;
  }
}
