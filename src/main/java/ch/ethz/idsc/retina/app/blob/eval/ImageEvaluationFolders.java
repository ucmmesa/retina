// code by jph
package ch.ethz.idsc.retina.app.blob.eval;

import java.io.File;

import ch.ethz.idsc.tensor.io.HomeDirectory;

public enum ImageEvaluationFolders {
  /** directory of the images to be labeled. NOTE: only the images should be in that directory */
  HANDLABEL(HomeDirectory.Pictures("handlabelimages")), //
  /** directory where evaluated images are saved */
  EVALUATED(HomeDirectory.Pictures("evaluatedimages")), //
  ;
  private final File folder;

  private ImageEvaluationFolders(File folder) {
    this.folder = StaticHelper.warningIfNotDirectory(folder);
  }

  /** @param subfolder name
   * @return directory */
  public File subfolder(String subfolder) {
    return StaticHelper.warningIfNotDirectory(new File(folder, subfolder));
  }
}
