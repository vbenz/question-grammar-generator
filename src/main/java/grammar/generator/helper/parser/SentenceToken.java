package grammar.generator.helper.parser;

import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.PropertyValue;
import eu.monnetproject.lemon.model.SynArg;
import grammar.structure.component.SentenceType;

import java.net.URI;
import java.util.Map;

public interface SentenceToken {
  void setPosition(int position);

  int getPosition();

  PropertyValue getPartOfSpeechValue();

  boolean isRootToken();

  Map<Property, PropertyValue> getPropertyMap();

  URI getLocalReference();

  boolean isConditionLabel();

  SynArg getSynArgForCondition();

  boolean isNestedSentenceTemplate();

  NestedSentenceTemplateData getNestedSentenceTemplateData();

  SynArg getSynArgValue();

  interface NestedSentenceTemplateData {
    SentenceType getType();

    String[] getArguments();
  }
}
