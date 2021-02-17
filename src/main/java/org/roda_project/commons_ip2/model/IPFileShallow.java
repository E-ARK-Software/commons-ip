package org.roda_project.commons_ip2.model;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.roda_project.commons_ip2.mets_v1_12.beans.FileType;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IPFileShallow implements IPFileInterface {

  private URL fileLocation;
  private FileType fileType;

  public IPFileShallow(URL fileLocation, FileType fileType) {
    super();
    this.fileLocation = fileLocation;
    this.fileType = fileType;
  }

  public URL getFileLocation() {
    return fileLocation;
  }

  public void setFileLocation(URL fileLocation) {
    this.fileLocation = fileLocation;
  }

  public FileType getFileType() {
    return fileType;
  }

  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

  @Override
  public List<String> getRelativeFolders() {
    throw new UnsupportedOperationException("IPFileShallow does not support this method");
  }

  @Override
  public String getFileName() {
      return null;
  }

  @Override
  public Path getPath() {
    throw new UnsupportedOperationException("IPFileShallow does not support this method");
  }
}
