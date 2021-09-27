package org.roda_project.commons_ip2.validator.reporter;

import org.roda_project.commons_ip2.validator.constants.Constants;
import org.roda_project.commons_ip2.validator.constants.ConstantsCSIPspec;
import org.roda_project.commons_ip2.validator.constants.ConstantsSIPspec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
/**
 * @author João Gomes <jgomes@keep.pt>
 */

public class ValidationReporter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationReporter.class);
  private Path outputFile;
  private OutputStream outputStream;
  private JsonGenerator jsonGenerator;
  private int success;
  private int errors;
  private int warnings;
  private int skipped;

  public ValidationReporter(Path path) {
    init(path);
  }

  public int getSuccess(){ return success;}

  public int getErrors(){ return errors;}

  public int getWarnings(){ return warnings;}

  public int getSkipped(){ return skipped;}

  public void countSuccess(){
    success++;
  }

  public void countWarnings(){
    warnings++;
  }

  public void countErrors(){
    errors++;
  }

  public void countSkipped() { skipped++; }

  private void init(Path path) {
    this.outputFile = path;
    this.success = 0;
    this.errors = 0;
    this.warnings = 0;
    if (!outputFile.toFile().exists()) {
      try {
        Files.createFile(outputFile);
      } catch (IOException e) {
        LOGGER.warn("Could not create report file in current working directory. Attempting to use a temporary file", e);
        try {
          outputFile = Files.createTempFile(Constants.VALIDATION_REPORT_PREFIX, ".json");
        } catch (IOException e1) {
          LOGGER.error("Could not create report temporary file. Reporting will not function.", e1);
        }
      }
    }
    else{
      try{
        Files.deleteIfExists(outputFile);
        try {
          Files.createFile(outputFile);
        } catch (IOException e) {
          LOGGER.warn("Could not create report file in current working directory. Attempting to use a temporary file", e);
          try {
            outputFile = Files.createTempFile(Constants.VALIDATION_REPORT_PREFIX, ".json");
          } catch (IOException e1) {
            LOGGER.error("Could not create report temporary file. Reporting will not function.", e1);
          }
        }
      } catch (IOException e) {
        LOGGER.warn("Could not eliminate old report file in current working directory.", e);
      }
    }

    if (outputFile != null) {
      try {
        outputStream = new BufferedOutputStream(new FileOutputStream(outputFile.toFile()));
        JsonFactory jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8);
        jsonGenerator.writeStartObject();
        // header object
        jsonGenerator.writeFieldName(Constants.VALIDATION_REPORT_HEADER_KEY_HEADER);
        jsonGenerator.writeStartObject();
        // header -> title
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_HEADER_KEY_TITLE, Constants.VALIDATION_REPORT_HEADER_TITLE);
        // header -> specifications
        jsonGenerator.writeFieldName(Constants.VALIDATION_REPORT_HEADER_KEY_SPECIFICATIONS);
        jsonGenerator.writeStartArray();
        // header -> specifications -> CSIP
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_KEY_ID, Constants.VALIDATION_REPORT_HEADER_CSIP_VERSION);
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_HEADER_SPECIFICATIONS_KEY_URL, Constants.VALIDATION_REPORT_HEADER_SPECIFICATIONS_URL_CSIP);
        jsonGenerator.writeEndObject();
        // header -> specifications -> SIP
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_KEY_ID, Constants.VALIDATION_REPORT_HEADER_SIP_VERSION);
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_HEADER_SPECIFICATIONS_KEY_URL, Constants.VALIDATION_REPORT_HEADER_SPECIFICATIONS_URL_SIP);
        jsonGenerator.writeEndObject();
        jsonGenerator.writeEndArray();
        // header -> version_commons_ip
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_VERSION_COMMONS_IP, Constants.VALIDATION_REPORT_SPECIFICATION_COMMONS_IP_VERSION);
        // header -> date (date of sip validation)
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_DATE, new org.joda.time.DateTime().toString());
        jsonGenerator.writeEndObject();
        // initialize validation array
        jsonGenerator.writeFieldName(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_VALIDATION);
        jsonGenerator.writeStartArray();
      } catch (IOException e) {
        LOGGER.error("Could not create an output stream for file '" + outputFile.normalize().toAbsolutePath().toString() + "'", e);
      }
    }
  }

  public void componentValidationResult(String specification,String id,String status,List<String> issues,String detail) {
    try {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_SPECIFICATION, specification);
      jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_KEY_ID, id);
      if(id.contains("C")){
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_NAME, ConstantsCSIPspec.getSpecificationName(id));
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_LOCATION, ConstantsCSIPspec.getSpecificationLocation(id));
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_DESCRIPTION, ConstantsCSIPspec.getSpecificationDescription(id));
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_CARDINALITY, ConstantsCSIPspec.getSpecificationCardinality(id));
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_LEVEL, ConstantsCSIPspec.getSpecificationLevel(id));
      }
      else{
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_NAME, ConstantsSIPspec.getSpecificationName(id));
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_LOCATION, ConstantsSIPspec.getSpecificationLocation(id));
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_DESCRIPTION, ConstantsSIPspec.getSpecificationDescription(id));
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_CARDINALITY, ConstantsSIPspec.getSpecificationCardinality(id));
        jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_LEVEL, ConstantsSIPspec.getSpecificationLevel(id));
      }
      jsonGenerator.writeFieldName(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_TESTING);
      jsonGenerator.writeStartObject();
      jsonGenerator.writeObjectField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_TESTING_OUTCOME, status);
      if(!detail.equals("")){
        jsonGenerator.writeObjectField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_TESTING_DETAIL,detail);
      }
      jsonGenerator.writeFieldName(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_TESTING_ISSUES);
      jsonGenerator.writeStartArray();
      for(String issue: issues){
        jsonGenerator.writeString(issue);
      }
      jsonGenerator.writeEndArray();
      jsonGenerator.writeEndObject();
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      LOGGER.error("Could not write specification " + specification + "result in file '", e);
    }
  }

  public void componentPathValidationResult(String id,String status,String detail){
    try {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_KEY_ID, id);
      jsonGenerator.writeFieldName(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_TESTING);
      jsonGenerator.writeStartObject();
      jsonGenerator.writeObjectField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_TESTING_OUTCOME, status);

      jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_TESTING_DETAIL, detail);
      jsonGenerator.writeEndObject();
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      LOGGER.error("Could not write result of" + id + "result in file '", e);
    }
  }

  public void componentValidationFinish(String status){
    try {
      jsonGenerator.writeEndArray();
      jsonGenerator.writeFieldName(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_SUMMARY);
      jsonGenerator.writeStartObject();
      jsonGenerator.writeNumberField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_SUCCESS, success);
      jsonGenerator.writeNumberField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_WARNINGS, warnings);
      jsonGenerator.writeNumberField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_ERRORS, errors);
      jsonGenerator.writeNumberField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_SKIPPED, skipped);
      jsonGenerator.writeStringField(Constants.VALIDATION_REPORT_SPECIFICATION_KEY_RESULT,status);
      jsonGenerator.writeEndObject();
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      LOGGER.error("Could not finish report!", e);
    }
  }

  public void validationResults(TreeMap<String, ReporterDetails> results){
    for(Map.Entry<String,ReporterDetails> entry: results.entrySet()){
      ReporterDetails details = entry.getValue();
      List<String> issues = details.getIssues();
      String detail = details.getDetail();
      if(details.isSkipped()){
        componentValidationResult(details.getSpecification(),entry.getKey(), Constants.VALIDATION_REPORT_SPECIFICATION_TESTING_OUTCOME_SKIPPED,issues,detail);
        skipped++;
      }
      else{
        if(details.isValid()){
          componentValidationResult(details.getSpecification(), entry.getKey(),Constants.VALIDATION_REPORT_SPECIFICATION_TESTING_OUTCOME_PASSED,issues,detail);
          success++;
        }
        else{
          componentValidationResult(details.getSpecification(), entry.getKey(),Constants.VALIDATION_REPORT_SPECIFICATION_TESTING_OUTCOME_FAILED,issues,detail);
          if(details.getSpecification().contains("C")) {
            if (ConstantsCSIPspec.getSpecificationLevel(entry.getKey()).equals("MUST")) {
              errors++;
            } else {
              if(!ConstantsCSIPspec.getSpecificationLevel(entry.getKey()).equals("MAY")) {
                warnings++;
              }
            }
          }
          else{
            if (ConstantsSIPspec.getSpecificationLevel(entry.getKey()).equals("MUST")) {
              errors++;
            } else {
              if(!ConstantsSIPspec.getSpecificationLevel(entry.getKey()).equals("MAY")) {
                warnings++;
              }
            }
          }
        }
      }
    }
  }

  public void close() {
    try {
      if (outputStream != null) {
        jsonGenerator.close();
        outputStream.close();
      }
    } catch (IOException e) {
      LOGGER.debug("Unable to close validation reporter file", e);
    } finally {
      if (outputStream != null) {
        LOGGER.info("A report was generated with a listing of information about the individual validations.");
        LOGGER.info("The report file is located at {}", outputFile.normalize().toAbsolutePath());
      } else {
        LOGGER.info("A report with a listing of information  about the individual validations could not be generated, please submit a bug report to help us fix this.");
      }
    }
  }
}
