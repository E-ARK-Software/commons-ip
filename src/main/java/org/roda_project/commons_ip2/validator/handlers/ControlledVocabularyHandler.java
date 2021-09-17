package org.roda_project.commons_ip2.validator.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

/**
 * @author João Gomes <jgomes@keep.pt>
 */

public class ControlledVocabularyHandler extends DefaultHandler {
  private String tag;
  private List<String> data;
  private boolean indexFound;
  private StringBuilder tmp = new StringBuilder();

  public ControlledVocabularyHandler(String tag, List<String> data) {
    this.tag = tag;
    this.data = data;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (qName.equals(tag)) {
      indexFound = true;
      tmp.setLength(0);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals(tag)) {
      data.add(tmp.toString());
      indexFound = false;
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    if (indexFound) {
      tmp.append(ch, start, length);
    }
  }

}
