package grammar.generator.helper;

import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.PropertyValue;
import grammar.generator.helper.sentencetemplates.AnnotatedVerb;
import grammar.sparql.SelectVariable;
import grammar.structure.component.DomainOrRangeType;
import grammar.structure.component.Language;
import grammar.structure.component.SentenceType;
import lexicon.LexicalEntryUtil;
import lexicon.LexiconSearch;
import net.lexinfo.LexInfo;
import util.exceptions.QueGGMissingFactoryClassException;

import java.util.ArrayList;
import java.util.List;

import static grammar.generator.helper.BindingConstants.BINDING_TOKEN_TEMPLATE;
import static lexicon.LexicalEntryUtil.getDeterminerTokenByNumber;

public class SentenceBuilderIntransitivePPEN implements SentenceBuilder {
  private final Language language;
  private final AnnotatedVerb annotatedVerb;
  private final LexicalEntryUtil lexicalEntryUtil;

  public SentenceBuilderIntransitivePPEN(
    AnnotatedVerb annotatedVerb,
    LexicalEntryUtil lexicalEntryUtil
  ) {
    this.language = Language.EN;
    this.annotatedVerb = annotatedVerb;
    this.lexicalEntryUtil = lexicalEntryUtil;
  }

  @Override
  public List<String> generateFullSentences(String bindingVariable, LexicalEntryUtil lexicalEntryUtil) throws QueGGMissingFactoryClassException {
    List<String> generatedSentences = new ArrayList<>();
    String sentence;
    LexInfo lexInfo = this.lexicalEntryUtil.getLexInfo();

    String preposition = this.lexicalEntryUtil.getPreposition();

    if (!lexInfo.getPropertyValue("infinitive").equals(annotatedVerb.getVerbFormMood())) {
      // Make simple sentence (Which river flows through $x?)
      if (lexInfo.getPropertyValue("singular").equals(annotatedVerb.getNumber())) {
        SubjectType subjectType = this.lexicalEntryUtil.getSubjectType(this.lexicalEntryUtil.getSelectVariable());
        String qWord = this.lexicalEntryUtil.getSubjectBySubjectType(subjectType, language, null);

        sentence = String.format(
          "%s %s %s %s?",
          qWord,
          annotatedVerb.getWrittenRepValue(),
          preposition,
          String.format(
            BINDING_TOKEN_TEMPLATE,
            bindingVariable,
            DomainOrRangeType.getMatchingType(this.lexicalEntryUtil.getConditionUriBySelectVariable(
              LexicalEntryUtil.getOppositeSelectVariable(this.lexicalEntryUtil.getSelectVariable())
            )).name(),
            SentenceType.NP
          )
        );
        generatedSentences.add(sentence);

      }
      // Make sentence using the specified domain or range property (Which museum exhibits $x?)
      // Only generate "Which <condition-label>" if condition label is a DBPedia entity
      if (!this.lexicalEntryUtil.hasInvalidDeterminerToken(this.lexicalEntryUtil.getSelectVariable())) {
        String conditionLabel = this.lexicalEntryUtil.getReturnVariableConditionLabel(this.lexicalEntryUtil.getSelectVariable());
        String determiner = this.lexicalEntryUtil.getSubjectBySubjectType(
          SubjectType.INTERROGATIVE_DETERMINER,
          language,
          null
        );
        String determinerToken = getDeterminerTokenByNumber(annotatedVerb.getNumber(), conditionLabel, determiner);
        sentence = String.format(
          "%s %s %s %s?",
          determinerToken,
          annotatedVerb.getWrittenRepValue(),
          preposition,
          String.format(
            BINDING_TOKEN_TEMPLATE,
            bindingVariable,
            DomainOrRangeType.getMatchingType(this.lexicalEntryUtil.getConditionUriBySelectVariable(
              LexicalEntryUtil.getOppositeSelectVariable(this.lexicalEntryUtil.getSelectVariable())
            )).name(),
            SentenceType.NP
          )
        );
        generatedSentences.add(sentence);
      }
    }
    return generatedSentences;
  }

  @Override
  public List<String> generateNP(String bindingVariable, String[] argument, LexicalEntryUtil lexicalEntryUtil) throws QueGGMissingFactoryClassException {
    List<String> generatedSentences = new ArrayList<>();
    String sentence;
    LexInfo lexInfo = this.lexicalEntryUtil.getLexInfo();
    String preposition = this.lexicalEntryUtil.getPreposition();

    if (lexInfo.getPropertyValue("infinitive").equals(annotatedVerb.getVerbFormMood())) {
      // E.g. Which cities does $x flow through?
      List<PropertyValue> numberList = new ArrayList<>();
      numberList.add(this.lexicalEntryUtil.getLexInfo().getPropertyValue("singular"));
      numberList.add(this.lexicalEntryUtil.getLexInfo().getPropertyValue("plural"));
      // Get verb "do"
      LexicalEntry component_do = new LexiconSearch(this.lexicalEntryUtil.getLexicon()).getReferencedResource("component_do");
      String form_does =
        component_do.getForms().stream()
                    .filter(lexicalForm -> lexicalForm.getProperty(lexInfo.getProperty("tense"))
                                                      .contains(lexInfo.getPropertyValue("present")))
                    .filter(lexicalForm -> lexicalForm.getProperty(lexInfo.getProperty("person"))
                                                      .contains(lexInfo.getPropertyValue("thirdPerson")))
                    .filter(lexicalForm -> lexicalForm.getProperty(lexInfo.getProperty("number"))
                                                      .contains(lexInfo.getPropertyValue("singular")))
                    .findFirst()
                    .orElseThrow()
                    .getWrittenRep().value;

      // opposite select variable
      SelectVariable oppositeSelectVariable = LexicalEntryUtil.getOppositeSelectVariable(this.lexicalEntryUtil.getSelectVariable());
      // get subjectType of this sentence's object
      SubjectType subjectType = this.lexicalEntryUtil.getSubjectType(oppositeSelectVariable);
      String qWord = this.lexicalEntryUtil.getSubjectBySubjectType(subjectType, language, null); // Who / What
      sentence = String.format(
        "%s %s %s %s %s?",
        qWord,
        form_does,
        bindingVariable, // won't use BINDING_TOKEN_TEMPLATE here because invalid sentences like "Which city does rivers crossed by $x flow through?" are generated - sentence needs an extension e.g. a PropertyValue (number) -> String map
        annotatedVerb.getWrittenRepValue(),
        preposition
      );
      generatedSentences.add(sentence);
      if (!this.lexicalEntryUtil.hasInvalidDeterminerToken(this.lexicalEntryUtil.getSelectVariable())) {
        for (PropertyValue number : numberList) {
          String conditionLabel = this.lexicalEntryUtil.getReturnVariableConditionLabel(oppositeSelectVariable);
          String determiner = this.lexicalEntryUtil.getSubjectBySubjectType(
            SubjectType.INTERROGATIVE_DETERMINER,
            language,
            null
          );
          String determinerToken = getDeterminerTokenByNumber(number, conditionLabel, determiner);
          sentence = String.format(
            "%s %s %s %s %s?",
            determinerToken,
            form_does,
            bindingVariable,
            annotatedVerb.getWrittenRepValue(),
            preposition
          );
          generatedSentences.add(sentence);
        }
      }
    }
    return generatedSentences;
  }
}
