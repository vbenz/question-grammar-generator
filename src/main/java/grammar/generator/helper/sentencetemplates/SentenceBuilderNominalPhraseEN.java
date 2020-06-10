package grammar.generator.helper.sentencetemplates;

import grammar.sparql.querycomponent.SelectVariable;

public class SentenceBuilderNominalPhraseEN {
//  private String questionWord;
  private String subject;
  private String verb;
  private String object;

  public SentenceBuilderNominalPhraseEN(String subject, String verb, String object) {
    this.subject = subject;
    this.verb = verb;
    this.object = object;
  }

  // What is a female thing?
  private String sentenceObjOfPropPassive () {
    return String.format("%s is a %s %s?", subject, verb, object);
  }

  public String getSentence(SelectVariable selectVariable, String form) {
    String sentence;
//    if (selectVariable.equals(OBJECT_OF_PROPERTY)) {
//      if (form.equals(LEXINFO.participle.getURI())) {
//        sentence = sentenceSubjOfPropPassive();
//      } else {
//        sentence = sentenceSubjOfPropActive();
//      }
//    } else if (selectVariable.equals(SUBJECT_OF_PROPERTY)&& form.equals(LEXINFO.participle.getURI())) {
      sentence = sentenceObjOfPropPassive();
//    } else {
//      sentence = "";
//    }
    return sentence;
  }
}
