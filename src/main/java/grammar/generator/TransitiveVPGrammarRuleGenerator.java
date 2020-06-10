package grammar.generator;

import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.PropertyValue;
import grammar.generator.helper.BindingConstants;
import grammar.generator.helper.SentenceConstants;
import grammar.generator.helper.SubjectType;
import grammar.generator.helper.sentencetemplates.SentenceBuilderTransitiveVPEN;
import grammar.sparql.querycomponent.SelectVariable;
import grammar.structure.component.FrameType;
import grammar.structure.component.Language;
import net.lexinfo.LexInfo;

import java.util.ArrayList;
import java.util.List;

public class TransitiveVPGrammarRuleGenerator extends GrammarRuleGeneratorRoot {

  public TransitiveVPGrammarRuleGenerator(Language language) {
    super(FrameType.VP, language, BindingConstants.DEFAULT_BINDING_VARIABLE);
  }

  @Override
  public List<String> generateSentences(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    List<String> generatedSentences = new ArrayList<>();
    SentenceConstants sentenceConstants = new SentenceConstants();
    LexInfo lexInfo = new LexInfo();
    Property tenseP = lexInfo.getProperty("verbFormMood");
    PropertyValue participlePV = lexInfo.getPropertyValue("participle");

    SubjectType subjectType = getSubjectType(selectVariable, lexicalEntry.getSenses().iterator().next().getReference().toString());
    String qWord = sentenceConstants.getLanguageQWordMap().get(getLanguage()).get(subjectType);

    List<LexicalForm> otherForms = new ArrayList<>(lexicalEntry.getOtherForms());
    for (LexicalForm lexicalForm : otherForms) {
      String verbFormMood = "";
      if (lexicalForm.getProperty(tenseP).contains(participlePV)) {
        verbFormMood = participlePV.toString();
        qWord = sentenceConstants.getLanguageQWordMap().get(getLanguage()).get(subjectType.equals(SubjectType.PERSON) ? SubjectType.PERSON2 : subjectType);
      }
      SentenceBuilderTransitiveVPEN sentenceBuilder = new SentenceBuilderTransitiveVPEN(qWord, lexicalForm.getWrittenRep().value, getBindingVariable());
      String sentence = sentenceBuilder.getSentence(selectVariable, verbFormMood);
      if (!sentence.isEmpty()) {
        generatedSentences.add(sentence);
      }
//        generatedSentences.add(String.format("By %s %s %s %s?", qWord, isString, getBindingVariable(), lexicalEntry.getAlternativeForms().get(s)));
    }
    return generatedSentences;
  }
}
