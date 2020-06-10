package grammar.generator.helper.sentencetemplates;

import grammar.sparql.querycomponent.SelectVariable;
import net.lexinfo.LexInfo;

import static grammar.sparql.querycomponent.SelectVariable.OBJECT_OF_PROPERTY;
import static grammar.sparql.querycomponent.SelectVariable.SUBJECT_OF_PROPERTY;

public class SentenceBuilderTransitiveVPEN {
//  private String questionWord;
  private String subject;
  private String verb;
  private String object;

  // By whom is $x written?
//  private final String sentenceSubjOfPropPassive = String.format("By %s is %s %s?", questionWord, subject, verb);

  // What is written by $x?
//  private final String sentenceObjOfPropPassive = String.format("%s is %s by %s?", questionWord, verb, object);

  // Who writes $x?
//  private final String sentenceSubjOfPropActive = String.format("%s %s %s?", questionWord, verb, object);

  public SentenceBuilderTransitiveVPEN(String subject, String verb, String object) {
    this.subject = subject;
    this.verb = verb;
    this.object = object;
  }

  // By whom is $x written?
  private String sentenceSubjOfPropPassive () {
    return String.format("By %s is %s %s?", subject, object, verb);
  }

  // What is written by $x?
  private String sentenceObjOfPropPassive () {
    return String.format("%s is %s by %s?", subject, verb, object);
  }

  // Who writes $x?
  private String sentenceSubjOfPropActive() {
    return String.format("%s %s %s?", subject, verb, object);
  }


  public String getSentence(SelectVariable selectVariable, String form) {
    String sentence;
    LexInfo lexInfo = new LexInfo();
    String participleURI = lexInfo.getPropertyValue("participle").getURI().toString();
    if (selectVariable.equals(OBJECT_OF_PROPERTY)) {
      if (form.equals(participleURI)) {
        sentence = sentenceSubjOfPropPassive();
      } else {
        sentence = sentenceSubjOfPropActive();
      }
    } else if (selectVariable.equals(SUBJECT_OF_PROPERTY)&& form.equals(participleURI)) {
      sentence = sentenceObjOfPropPassive();
    } else {
      sentence = "";
    }
    return sentence;
  }
}
