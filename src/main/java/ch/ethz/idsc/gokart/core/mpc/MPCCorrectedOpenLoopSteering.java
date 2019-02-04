// code by mh
package ch.ethz.idsc.gokart.core.mpc;

import java.util.Objects;

import ch.ethz.idsc.tensor.Scalar;

public class MPCCorrectedOpenLoopSteering extends MPCSteering {
  MPCStateEstimationProvider mpcStateProvider;
  MPCActiveCompensationLearning mpcActiveCompensationLearning = MPCActiveCompensationLearning.getInstance();
  MPCOptimizationConfig config = MPCOptimizationConfig.GLOBAL;

  @Override
  public Scalar getSteering(Scalar time) {
    Scalar controlTime = time.add(config.steerAntiLag);
    ControlAndPredictionStep cnpStep = getStep(controlTime);
    if (Objects.isNull(cnpStep))
      return null;
    Scalar timeSinceLastStep = getTimeSinceLastStep(controlTime);
    Scalar rampUp = timeSinceLastStep.multiply(cnpStep.control.getudotS());
    return cnpStep.state.getS().add(rampUp)//
        .multiply(mpcActiveCompensationLearning.steeringCorrection);
  }

  @Override
  public Scalar getDotSteering(Scalar time) {
    Scalar controlTime = time.add(config.steerAntiLag);
    ControlAndPredictionStep cnpStep = getStep(controlTime);
    if (Objects.isNull(cnpStep))
      return null;
    return cnpStep.control.getudotS()//
        .multiply(mpcActiveCompensationLearning.steeringCorrection);
  }

  @Override
  public void getControlAndPredictionSteps(ControlAndPredictionSteps controlAndPredictionSteps) {
    cns = controlAndPredictionSteps;
  }

  @Override
  public void setStateProvider(MPCStateEstimationProvider mpcstateProvider) {
    this.mpcStateProvider = mpcstateProvider;
  }
}