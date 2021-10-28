package org.roda_project.commons_ip2.validator.reporter;

import org.roda_project.commons_ip2.validator.component.fileComponent.StructureComponentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author João Gomes <jgomes@keep.pt>
 */
public class ReporterDetails {
  private String detail;
  private boolean valid;
  private final List<String> issues;
  private String specification;
  private boolean skipped;
  private int errors;

  public ReporterDetails() {
    this.detail = "";
    this.valid = true;
    this.issues = new ArrayList<>();
    this.errors = 0;
  }

  public ReporterDetails(String specification, String issue, boolean valid, boolean skipped) {
    this.detail = "";
    this.valid = valid;
    this.issues = new ArrayList<>();
    this.issues.add(issue);
    this.errors = 0;
    this.specification = specification;
    this.skipped = skipped;
  }

  public ReporterDetails(String specification,List<String> issues, boolean valid,boolean skipped){
    this.detail = "";
    this.specification = specification;
    this.issues = new ArrayList<>(issues);
    this.errors = 0;
    this.valid = valid;
    this.skipped = skipped;
  }

  public ReporterDetails(ReporterDetails clone) {
    this(clone.getSpecification(),clone.getIssues(), clone.isValid(), clone.isSkipped());
  }

  public ReporterDetails clone() {
    return new ReporterDetails(this);
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String message) {
    this.detail = message;
  }

  public String getSpecification() {
    return specification;
  }

  public ReporterDetails setSpecification(String specification) {
    this.specification = specification;
    return this;
  }

  public List<String> getIssues() {
    return this.issues;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public boolean isSkipped() {
    return skipped;
  }

  public void setSkipped(boolean skipped) {
    this.skipped = skipped;
  }

  public void addIssue(String issue) {
    this.issues.add(issue);
  }

  public void addIssues(List<String> issues) {
    this.issues.addAll(issues);
  }

}
