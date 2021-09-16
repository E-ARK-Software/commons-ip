package org.roda_project.commons_ip2.validator.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author João Gomes <jgomes@keep.pt>
 */
public class FolderManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(FolderManager.class);

  private File folder = null;

  public boolean checkIfExistsRootMetsFile(Path path) {
    boolean found = false;
    folder = path.toFile();
    for (File f : folder.listFiles()) {
      if (f.getName().equals("METS.xml")) {
        found = true;
      }
    }
    return found;
  }

  public InputStream getMetsRootInputStream(Path path) throws FileNotFoundException {
    folder = path.toFile();
    String metsPath = null;
    for (File f : folder.listFiles()) {
      if (f.getName().equals("METS.xml")) {
        metsPath = f.getPath();
      }
    }
    if (metsPath == null) {
      LOGGER.debug("METS.xml not Found");
      throw new FileNotFoundException("METS.xml not Found");
    }
    return new FileInputStream(metsPath);
  }

  public String getSipRootFolderName(Path path) {
    String[] tmp = path.toString().split("/");
    return tmp[tmp.length - 1];
  }

  public boolean checkPathExists(Path path) throws IOException {
    return Files.exists(path);
  }

  public boolean verifyChecksum(Path path, String alg, String checksum)
    throws IOException, NoSuchAlgorithmException {
    boolean valid = true;

    if (!Files.exists(path)) {
      valid = false;
    } else {
      if (Files.size(path) == 0) {
        valid = false;
      } else {
        InputStream stream = new FileInputStream(path.toFile());
        MessageDigest messageDigest = MessageDigest.getInstance(alg);
        byte[] buffer = new byte[8192];
        int numOfBytesRead;
        while ((numOfBytesRead = stream.read(buffer)) > 0) {
          messageDigest.update(buffer, 0, numOfBytesRead);
        }
        byte[] hash = messageDigest.digest();
        String fileChecksum = DatatypeConverter.printHexBinary(hash);
        if (!checksum.equals(fileChecksum)) {
          valid = false;
        }
        stream.close();
      }
    }
    return valid;
  }

  public boolean verifySize(Path path, Long metsSize) throws IOException {
    boolean valid = true;
    if (path == null) {
      valid = false;
    } else {
      if (Files.size(path) != metsSize) {
        valid = false;
      }
    }
    return valid;
  }

  public boolean verifyMetadataFilesFolder(Path path, String name) {
    File[] folder = path.toFile().listFiles();
    if (folder != null) {
      for (File f : folder) {
        if (f.getName().equals("metadata")) {
          if (f.isDirectory()) {
            File[] metadataFiles = f.listFiles();
            if (metadataFiles == null) {
              return false;
            } else {
              if (metadataFiles.length == 0) {
                return false;
              } else {
                for (File metadata : metadataFiles) {
                  if (metadata.getName().equals(name)) {
                    return metadata.isDirectory();
                  }
                }
              }
            }

          } else {
            return false;
          }
        }
      }
    }
    return false;
  }

  public int countMetadataFiles(Path path, String name) {
    int count = 0;
    File[] folder = path.toFile().listFiles();
    if (folder != null) {
      for (File f : folder) {
        if (f.getName().equals("metadata")) {
          if (f.isDirectory()) {
            File[] metadataFiles = f.listFiles();
            if (metadataFiles != null) {
              if (metadataFiles.length != 0) {
                for (File metadata : metadataFiles) {
                  if (metadata.getName().equals(name)) {
                    if (metadata.isDirectory()) {
                      File[] descriptiveFiles = metadata.listFiles();
                      if (descriptiveFiles != null) {
                        count = descriptiveFiles.length;
                      }
                    }
                  }
                }
              }
            }

          }
        }
      }
    }
    return count;
  }

  public HashMap<String, InputStream> getSubMets(Path path) throws FileNotFoundException {
    HashMap<String, InputStream> subMets = new HashMap<>();
    File[] folder = path.toFile().listFiles();
    if (folder != null) {
      for (File f : folder) {
        if (f.getName().equals("representations")) {
          if (f.isDirectory()) {
            File[] representations = f.listFiles();
            if (representations != null) {
              if (representations.length != 0) {
                for (File representation : representations) {
                  if (representation.isDirectory()) {
                    File[] rep = representation.listFiles();
                    if (rep != null) {
                      if (rep.length != 0) {
                        for (File r : rep) {
                          if (r.getName().equals("METS.xml")) {
                            InputStream subStream = new FileInputStream(r.getPath());
                            subMets.put(r.getPath(), subStream);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return subMets;
  }

  public boolean checkDirectory(Path path) throws IOException {
    return Files.exists(path);
  }

  public Boolean checkRootFolderName(Path path, String OBJECTID) {
    return path.endsWith(OBJECTID);
  }

  public boolean checkIfExistsFolderInRoot(Path path, String folder) {
    File[] root = path.toFile().listFiles();
    for (File file : root) {
      if (file.getName().equals(folder)) {
        if (file.isDirectory()) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean checkIfExistsFolderInside(Path path, String rootFolder, String insideFolder) {
    File[] root = path.toFile().listFiles();
    for (File file : root) {
      if (file.getName().equals(rootFolder)) {
        if (file.isDirectory()) {
          File[] insideFiles = file.listFiles();
          for (File f : insideFiles) {
            if (f.getName().equals(insideFolder)) {
              if (f.isDirectory()) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  public boolean verifyIfExistsFilesInFolder(Path path, String folder) {
    File[] root = path.toFile().listFiles();
    for (File file : root) {
      if (file.getName().equals(folder)) {
        if (file.isDirectory()) {
          File[] insideFiles = file.listFiles();
          for (File f : insideFiles) {
            if (!f.isDirectory()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public boolean checkIfExistsFolderInsideRepresentation(Path path, String folder) {
    File[] root = path.toFile().listFiles();
    for (File file : root) {
      if (file.getName().equals("representations")) {
        if (file.isDirectory()) {
          File[] insideFiles = file.listFiles();
          for (File f : insideFiles) {
            if (f.isDirectory()) {
              File[] representationFiles = file.listFiles();
              for (File representationFile : representationFiles) {
                if (representationFile.getName().equals(folder)) {
                  if (representationFile.isDirectory()) {
                    return true;
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  public boolean checkIfExistsFilesInsideRepresentationFolder(Path path) {
    File[] root = path.toFile().listFiles();
    for (File file : root) {
      if (file.getName().equals("representations")) {
        if (file.isDirectory()) {
          File[] insideFiles = file.listFiles();
          for (File f : insideFiles) {
            if (f.isDirectory()) {
              File[] representationFiles = file.listFiles();
              for (File representationFile : representationFiles) {
                if (!representationFile.isDirectory()) {
                  if (!representationFile.getName().equals("METS.xml")) {
                    return true;
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  public boolean checkIfExistsSubMets(Path path) {
    File[] root = path.toFile().listFiles();
    int countSubMets = 0;
    int countRepresentationsFolder = 0;
    for (File file : root) {
      if (file.getName().equals("representations")) {
        if (file.isDirectory()) {
          File[] insideFiles = file.listFiles();
          for (File f : insideFiles) {
            if (f.isDirectory()) {
              countRepresentationsFolder++;
              File[] representationFiles = f.listFiles();
              for (File representationFile : representationFiles) {
                if (representationFile.getName().equals("METS.xml")) {
                    countSubMets++;
                  }
                }
            }
          }
        }
      }
    }
    return countSubMets == countRepresentationsFolder;
  }

  public List<String> getRepresentationsFoldersNames(Path path) {
    List<String> representationsFoldersNames = new ArrayList<>();
    File[] rootFiles = path.toFile().listFiles();
    for(File rootFile: rootFiles ){
      if(rootFile.getName().equals("representations")){
        if(rootFile.isDirectory()){
          File[] representationsFiles = rootFile.listFiles();
          for(File representation: representationsFiles){
            if(representation.isDirectory()){
              representationsFoldersNames.add(representation.getName());
            }
          }
        }
      }
    }
    return representationsFoldersNames;
  }

  public int countFilesInsideRepresentations(Path path) {
    int count = 0;
    File[] rootFiles = path.toFile().listFiles();
    for(File rootFile: rootFiles ){
      if(rootFile.getName().equals("representations")){
        if(rootFile.isDirectory()){
          File[] representationsFiles = rootFile.listFiles();
          for(File representation: representationsFiles){
            if(!representation.isDirectory()){
              count++;
            }
          }
        }
      }
    }
    return count;
  }

  public InputStream getInputStream(Path path) throws FileNotFoundException {
    if(path == null){
      LOGGER.debug("File not Found");
      throw new FileNotFoundException("File not Found");
    }
    return new FileInputStream(path.toFile());
  }
}
