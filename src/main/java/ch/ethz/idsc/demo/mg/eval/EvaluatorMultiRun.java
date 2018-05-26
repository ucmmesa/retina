// code by mg
package ch.ethz.idsc.demo.mg.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.idsc.demo.mg.pipeline.PipelineConfig;
import ch.ethz.idsc.demo.mg.util.CSVUtil;

/** compares a bunch of estimated runs against the ground truth by initializing one TrackingEvaluatorSingleRun per
 * estimated run. */
/* package */ class EvaluatorMultiRun {
  private final PipelineConfig pipelineConfig;
  private final String evaluationResultFileName;
  private final File evaluationResultFile;
  private final List<double[]> collectedResults;
  private final int iterationLength;

  EvaluatorMultiRun(PipelineConfig pipelineConfig) {
    this.pipelineConfig = pipelineConfig;
    evaluationResultFileName = pipelineConfig.evaluationResultFileName.toString();
    evaluationResultFile = EvaluationFileLocations.evalResults(evaluationResultFileName);
    iterationLength = pipelineConfig.iterationLength.number().intValue();
    collectedResults = new ArrayList<>(iterationLength);
  }

  private void multiRun() {
    for (int i = 0; i < iterationLength; i++) {
      // to initialize singleRun, only the estimatedLabelFileName needs to be changed
      // TODO this needs to be similar to the fileNames defined in PipelineSetup::iterate() maybe there is a more elegant option
      int newTau = 1000 + 1000 * i;
      String newEstimatedLabelFileName = pipelineConfig.logFileName.toString() + "_tau_" + newTau;
      pipelineConfig.estimatedLabelFileName = "Dubi15a_tau/" + newEstimatedLabelFileName;
      // initialize singleRun object and run evaluation
      EvaluatorSingleRun singleRun = new EvaluatorSingleRun(pipelineConfig);
      singleRun.runEvaluation();
      // collect results
      double[] results = new double[] { newTau, singleRun.getResults()[0], singleRun.getResults()[1] };
      collectedResults.add(results);
    }
    // TODO probably also save pipelineConfig that was used?
    CSVUtil.saveToCSV(evaluationResultFile, collectedResults);
    System.out.println("Successfully saved evaluation results to " + evaluationResultFileName);
  }

  private void summarizeResults() {
    for (int i = 0; i < iterationLength; i++) {
      System.out.println("average recall is " + collectedResults.get(i)[1]);
      System.out.println("average precision is " + collectedResults.get(i)[2]);
    }
  }

  // standalone application
  public static void main(String[] args) {
    PipelineConfig pipelineConfig = new PipelineConfig();
    EvaluatorMultiRun test = new EvaluatorMultiRun(pipelineConfig);
    test.multiRun();
    test.summarizeResults();
  }
}
