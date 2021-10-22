package org.roda_project.commons_ip2.validator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.roda_project.commons_ip2.validator.aipComponents.aipFileSectionComponent.AipFileSectionComponent;
import org.roda_project.commons_ip2.validator.common.InstatiateMets;
import org.roda_project.commons_ip2.validator.component.MetsValidator;
import org.roda_project.commons_ip2.validator.component.administritiveMetadataComponent.AdministritiveMetadataComponentValidator;
import org.roda_project.commons_ip2.validator.component.descriptiveMetadataComponent.DescriptiveMetadataComponentValidator;
import org.roda_project.commons_ip2.validator.component.fileComponent.StructureComponentValidator;
import org.roda_project.commons_ip2.validator.component.fileSectionComponent.FileSectionComponentValidator;
import org.roda_project.commons_ip2.validator.component.metsrootComponent.MetsComponentValidator;
import org.roda_project.commons_ip2.validator.component.metsrootComponent.MetsHeaderComponentValidator;
import org.roda_project.commons_ip2.validator.component.structuralMapComponent.StructuralMapComponentValidator;
import org.roda_project.commons_ip2.validator.constants.Constants;
import org.roda_project.commons_ip2.validator.constants.ConstantsCSIPspec;
import org.roda_project.commons_ip2.validator.observer.ValidationObserver;
import org.roda_project.commons_ip2.validator.reporter.ReporterDetails;
import org.roda_project.commons_ip2.validator.reporter.ValidationReportOutputJson;
import org.roda_project.commons_ip2.validator.sipComponents.sipFileSectionComponent.SipFileSectionComponent;
import org.roda_project.commons_ip2.validator.sipComponents.sipMetsRootComponent.SipMetsComponent;
import org.roda_project.commons_ip2.validator.sipComponents.sipMetsRootComponent.SipMetsHdrComponent;
import org.roda_project.commons_ip2.validator.state.MetsValidatorState;
import org.roda_project.commons_ip2.validator.state.StructureValidatorState;
import org.roda_project.commons_ip2.validator.utils.ResultsUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author João Gomes <jgomes@keep.pt>
 */

public class EARKSIPValidator {
  private final Path earksipPath;

  private final ValidationReportOutputJson validationReportOutputJson;
  private final StructureComponentValidator structureComponent;
  private final StructureValidatorState structureValidatorState;
  private final List<MetsValidator> csipComponents = new ArrayList<>();
  private final List<MetsValidator> sipComponents = new ArrayList<>();
  private final List<MetsValidator> aipComponents = new ArrayList<>();
  private final MetsValidatorState metsValidatorState;

  public EARKSIPValidator(ValidationReportOutputJson reportOutputJson)
    throws IOException, ParserConfigurationException, SAXException {

    this.earksipPath = reportOutputJson.getSipPath().toAbsolutePath().normalize();

    this.validationReportOutputJson = reportOutputJson;

    this.structureValidatorState = new StructureValidatorState(
      reportOutputJson.getSipPath().toAbsolutePath().normalize());
    this.structureComponent = new StructureComponentValidator();
    this.metsValidatorState = new MetsValidatorState();
    setupComponents();
  }

  private void setupComponents() throws IOException, ParserConfigurationException, SAXException {
    this.csipComponents.add(new MetsComponentValidator());
    this.csipComponents.add(new MetsHeaderComponentValidator());
    this.csipComponents.add(new DescriptiveMetadataComponentValidator());
    this.csipComponents.add(new AdministritiveMetadataComponentValidator());
    this.csipComponents.add(new FileSectionComponentValidator());
    this.csipComponents.add(new StructuralMapComponentValidator());

    this.sipComponents.add(new SipMetsComponent());
    this.sipComponents.add(new SipMetsHdrComponent());
    this.sipComponents.add(new SipFileSectionComponent());

    this.aipComponents.add(new AipFileSectionComponent());
  }

  public void addObserver(ValidationObserver observer) {
    structureComponent.addObserver(observer);
    csipComponents.forEach(c -> c.addObserver(observer));
    sipComponents.forEach(c -> c.addObserver(observer));
  }

  public void removeObserver(ValidationObserver observer) {
    structureComponent.removeObserver(observer);
    csipComponents.forEach(c -> c.removeObserver(observer));
    sipComponents.forEach(c -> c.removeObserver(observer));
  }

  public boolean validate() {
    structureComponent.notifyObserversIPValidationStarted();
    try {
      Map<String, ReporterDetails> structureValidationResults = structureComponent.validate(structureValidatorState);
      validationReportOutputJson.getResults().putAll(structureValidationResults);

      if (validationReportOutputJson.validFileComponent()) {
        Map<String, InputStream> subMets;
        if (structureValidatorState.isZipFileFlag()) {
          metsValidatorState.setMetsFiles(structureValidatorState.getZipManager().getFiles(earksipPath));
          subMets = structureValidatorState.getZipManager().getSubMets(earksipPath);
        } else {
          subMets = structureValidatorState.getFolderManager().getSubMets(earksipPath);
        }

        if (subMets.size() > 0) {
          validateSubMets(subMets, structureValidatorState.isZipFileFlag());
        }
        validateRootMets();

        ReporterDetails csipStr0 = new ReporterDetails(Constants.VALIDATION_REPORT_HEADER_CSIP_VERSION, "", true,
          false);
        csipStr0.setSpecification(Constants.VALIDATION_REPORT_HEADER_CSIP_VERSION);
        validationReportOutputJson.getResults().put(ConstantsCSIPspec.VALIDATION_REPORT_SPECIFICATION_CSIP0_ID,
          csipStr0);
      }
      writeReport(true);

    } catch (IOException | JAXBException | SAXException e) {
      StringBuilder message = new StringBuilder();

      Throwable cause = e;
      if (e.getMessage() != null) {
        message.append("[").append(e.getClass().getSimpleName()).append("] ").append(e.getMessage());
      }
      while (cause.getCause() != null) {
        cause = cause.getCause();
        if (message.length() > 0) {
          message.append(" caused by ");
        }

        message.append("[").append(cause.getClass().getSimpleName()).append("] ").append(cause.getMessage());

        if (cause instanceof SAXParseException) {
          SAXParseException e1 = (SAXParseException) cause;
          message.append(" (line: ").append(e1.getLineNumber()).append(", column: ").append(e1.getColumnNumber())
            .append(") - ");
        }
      }

      ReporterDetails csipStr0 = new ReporterDetails(Constants.VALIDATION_REPORT_HEADER_CSIP_VERSION,
        message.toString(), false, false);
      csipStr0.setSpecification(Constants.VALIDATION_REPORT_HEADER_CSIP_VERSION);
      validationReportOutputJson.getResults().put(ConstantsCSIPspec.VALIDATION_REPORT_SPECIFICATION_CSIP0_ID, csipStr0);

      writeReport(false);
    }
    return validationReportOutputJson.getErrors() == 0;
  }

  private void validateComponents() throws IOException {
    for (MetsValidator component : csipComponents) {
      Map<String, ReporterDetails> componentResults = component.validate(structureValidatorState, metsValidatorState);
      ResultsUtils.mergeResults(validationReportOutputJson.getResults(), componentResults);
    }
    validateIpTypeExtendedComponents();
  }

  private void validateSubMets(Map<String, InputStream> subMets, boolean isZip)
    throws IOException, JAXBException, SAXException {
    for (Map.Entry<String, InputStream> entry : subMets.entrySet()) {
      InstatiateMets instatiateMets = new InstatiateMets(entry.getValue());
      metsValidatorState.setMets(instatiateMets.instatiateMetsFile());
      metsValidatorState.setIpType(metsValidatorState.getMets().getMetsHdr().getOAISPACKAGETYPE());
      setupMetsValidatorState(entry.getKey(), isZip, false);
      validateComponents();
    }
  }

  private void validateRootMets() throws IOException, JAXBException, SAXException {
    InputStream metsRootStream;
    String ipPath;
    if (structureValidatorState.isZipFileFlag()) {
      metsRootStream = structureValidatorState.getZipManager().getMetsRootInputStream(earksipPath);
      ipPath = earksipPath.toString();
    } else {
      metsRootStream = structureValidatorState.getFolderManager().getMetsRootInputStream(earksipPath);
      ipPath = earksipPath.resolve("METS.xml").toString();
    }

    InstatiateMets metsRoot = new InstatiateMets(metsRootStream);
    metsValidatorState.setMetsPath(ipPath);
    metsValidatorState.setMetsName(ipPath);
    metsValidatorState.setIsRootMets(true);
    metsValidatorState.setMets(metsRoot.instatiateMetsFile());
    validateComponents();
  }

  private void setupMetsValidatorState(String key, boolean isZip, boolean isRootMets) {
    this.metsValidatorState.setMetsName(key);
    this.metsValidatorState.setIsRootMets(isRootMets);
    if (isZip) {
      StringBuilder metsPath = new StringBuilder();
      for (String path : key.split("/")) {
        if (!path.equals("METS.xml")) {
          metsPath.append(path).append("/");
        }
      }
      this.metsValidatorState.setMetsPath(metsPath.toString());
    } else {
      this.metsValidatorState.setMetsPath(Paths.get(key).getParent().toString());
    }
  }

  public void notifyIndicatorsObservers() {
    structureComponent.notifyIndicators(this.validationReportOutputJson.getErrors(),
      this.validationReportOutputJson.getSuccess(), this.validationReportOutputJson.getWarnings(),
      this.validationReportOutputJson.getNotes(), this.validationReportOutputJson.getSkipped());
  }

  private void validateIpTypeExtendedComponents() throws IOException {
    if (metsValidatorState.getIpType().equals("SIP")) {
      aipComponents.clear();
      for (MetsValidator component : sipComponents) {
        if (component instanceof SipFileSectionComponent) {
          ((SipFileSectionComponent) component).setIsToValidate(ResultsUtils.isResultValid(
            validationReportOutputJson.getResults(), ConstantsCSIPspec.VALIDATION_REPORT_SPECIFICATION_CSIP58_ID));
        }
        if (component instanceof SipMetsHdrComponent) {
          ((SipMetsHdrComponent) component).setIsToValidateMetsHdr(ResultsUtils.isResultValid(
            validationReportOutputJson.getResults(), ConstantsCSIPspec.VALIDATION_REPORT_SPECIFICATION_CSIP117_ID));
          if (validationReportOutputJson.getResults()
            .get(ConstantsCSIPspec.VALIDATION_REPORT_SPECIFICATION_CSIP10_ID) != null) {
            ((SipMetsHdrComponent) component).setIsToValidateAgents(ResultsUtils.isResultValid(
              validationReportOutputJson.getResults(), ConstantsCSIPspec.VALIDATION_REPORT_SPECIFICATION_CSIP10_ID));
          }
        }
        Map<String, ReporterDetails> sipComponentResults = component.validate(structureValidatorState,
          metsValidatorState);
        ResultsUtils.mergeResults(validationReportOutputJson.getResults(), sipComponentResults);
      }
    } else {
      if (metsValidatorState.getIpType().equals("AIP")) {
        sipComponents.clear();
        for (MetsValidator component : aipComponents) {
          if (component instanceof AipFileSectionComponent) {
            ((AipFileSectionComponent) component).setIsToValidate(ResultsUtils.isResultValid(
              validationReportOutputJson.getResults(), ConstantsCSIPspec.VALIDATION_REPORT_SPECIFICATION_CSIP58_ID));
          }
          Map<String, ReporterDetails> aipComponentResults = component.validate(structureValidatorState,
                  metsValidatorState);
          ResultsUtils.mergeResults(validationReportOutputJson.getResults(), aipComponentResults);
        }
      }
    }
  }

  private void writeReport(boolean isSchemaValid){
      if(isSchemaValid){
        validationReportOutputJson.setIpType(metsValidatorState.getIpType());
      }
      else{
        validationReportOutputJson.setIpType("");
      }
    validationReportOutputJson.init();
    validationReportOutputJson.validationResults();
    validationReportOutputJson.writeFinalResult();
    notifyIndicatorsObservers();
    validationReportOutputJson.close();
    structureComponent.notifyObserversIPValidationFinished();
  }
}
