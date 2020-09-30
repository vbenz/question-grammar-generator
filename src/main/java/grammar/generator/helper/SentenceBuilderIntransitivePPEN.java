package grammar.generator.helper;

import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
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
import grammar.generator.helper.parser.SentenceToken;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.isNull;
import java.util.Optional;
import java.util.Set;
import static lexicon.LexicalEntryUtil.getDeterminerTokenByNumber;

public class SentenceBuilderIntransitivePPEN implements SentenceBuilder {

    private final Language language;
    private final AnnotatedVerb annotatedVerb;
    private final LexicalEntryUtil lexicalEntryUtil;
    //Since many things are hard coded or solved by if else so this is a temporary solution. 
    //the logic of Sentencebuilder works almost same way so it should be merged NNP
    private String sentenceTemplate = "TemporalDeterminer verb(reference:component_be) determiner(reference:component_the) noun(condition:subject) VP(temporalAdjunct)?";

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
        String sentence, preposition;
        DomainOrRangeType domainOrRangeType;
        String binding = null, verb = null;
        LexInfo lexInfo = this.lexicalEntryUtil.getLexInfo();

        preposition = this.lexicalEntryUtil.getPreposition();

        //This has to be descided from the range..temporary code for solving problem.
        Boolean flag = true;

        //The code was not written to use sentence template to work for intransitive verb. This has to be integreated. This is temporary solution.
        if (sentenceTemplate.contains("temporalAdjunct")) {
            domainOrRangeType = DomainOrRangeType.YEAR;
        } else {
            domainOrRangeType = DomainOrRangeType.THING;
        }

        if (!lexInfo.getPropertyValue("infinitive").equals(annotatedVerb.getVerbFormMood())) {
            // Make simple sentence (Which river flows through $x?)
            if (lexInfo.getPropertyValue("singular").equals(annotatedVerb.getNumber())) {
                SubjectType subjectType = this.lexicalEntryUtil.getSubjectType(this.lexicalEntryUtil.getSelectVariable(), domainOrRangeType);
                String qWord = this.lexicalEntryUtil.getSubjectBySubjectType(subjectType, language, null);

                String bindingString = DomainOrRangeType.getMatchingType(this.lexicalEntryUtil.getConditionUriBySelectVariable(
                        LexicalEntryUtil.getOppositeSelectVariable(this.lexicalEntryUtil.getSelectVariable())
                )).name();
                binding = String.format(
                        BINDING_TOKEN_TEMPLATE,
                        bindingVariable,
                        bindingString,
                        SentenceType.NP
                );

                if (flag) {
                    sentence = String.format(
                            "%s %s %s %s %s?",
                            qWord,
                            "was",
                            "the",
                            binding,
                            annotatedVerb.getWrittenRepValue()
                    );
                } else {
                    sentence = String.format(
                            "%s %s %s %s?",
                            qWord,
                            annotatedVerb.getWrittenRepValue(),
                            preposition,
                            binding
                    );
                }

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

                String bindingString = DomainOrRangeType.getMatchingType(this.lexicalEntryUtil.getConditionUriBySelectVariable(
                        LexicalEntryUtil.getOppositeSelectVariable(this.lexicalEntryUtil.getSelectVariable())
                )).name();
                binding = String.format(
                        BINDING_TOKEN_TEMPLATE,
                        bindingVariable,
                        bindingString,
                        SentenceType.NP
                );

                if (flag) {
                    sentence = String.format(
                            "%s %s %s %s %s?",
                            determinerToken,
                            "was",
                            "the",
                            binding,
                            annotatedVerb.getWrittenRepValue()
                    );

                } else {
                    sentence = String.format(
                            "%s %s %s %s?",
                            determinerToken,
                            annotatedVerb.getWrittenRepValue(),
                            preposition,
                            binding
                    );
                }

                generatedSentences.add(sentence);
            }
        }
        return generatedSentences;
    }

    /*@Override
  public List<String> generateFullSentences(String bindingVariable, LexicalEntryUtil lexicalEntryUtil) throws QueGGMissingFactoryClassException {
    List<String> generatedSentences = new ArrayList<>();
    String sentence,preposition;
    LexInfo lexInfo = this.lexicalEntryUtil.getLexInfo();

    preposition = this.lexicalEntryUtil.getPreposition();

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
  }*/
    @Override
    public List<String> generateNP(String bindingVariable, String[] argument, LexicalEntryUtil lexicalEntryUtil) throws QueGGMissingFactoryClassException {
        List<String> generatedSentences = new ArrayList<>();
        String sentence;
        LexInfo lexInfo = this.lexicalEntryUtil.getLexInfo();
        String preposition = this.lexicalEntryUtil.getPreposition();
        DomainOrRangeType domainOrRangeType;
        //This is a temporary solution
        String source="http://www.lexinfo.net/ontology/2.0/lexinfo#";
      

        if (sentenceTemplate.contains("temporalAdjunct")) {
            domainOrRangeType = DomainOrRangeType.THING;
        } else {
            domainOrRangeType = DomainOrRangeType.PERSON;
        }

        if (!lexInfo.getPropertyValue("infinitive").equals(annotatedVerb.getVerbFormMood())) {
            // E.g. Which cities does $x flow through?
            List<PropertyValue> numberList = new ArrayList<>();
            numberList.add(this.lexicalEntryUtil.getLexInfo().getPropertyValue("singular"));
            numberList.add(this.lexicalEntryUtil.getLexInfo().getPropertyValue("plural"));
            // Get verb "do"
            LexicalEntry component_do = new LexiconSearch(this.lexicalEntryUtil.getLexicon()).getReferencedResource("component_aux_object_past");
         
            //LexicalEntry preposition_comp = new LexiconSearch(this.lexicalEntryUtil.getLexicon()).getReferencedResource("in");
            
            /*preposition=  preposition_comp.getForms().stream()
                                .filter(lexicalForm -> lexicalForm.getProperty(lexInfo.getProperty("partOfSpeech"))
                                .contains(lexInfo.getPropertyValue("preposition")))
                                .findFirst()
                                .orElseThrow()
                                .getWrittenRep().value;*/
            
            //horrible coding...many different ways to do the same thing in all Gramatical sentence
            
            //Collection<LexicalForm> LexicalForms=component_do.getForms();
            
            //temporarySolutions..
            //Set<String> numbers=new HashSet<String>();
            List<String> auxilaries = new ArrayList<String>();
            //numbers.add("singular");
            //numbers.add("plural");
            
            for (PropertyValue number : numberList) {
                String numberStr=number.toString().replace(source, "");
                auxilaries.add(
                        component_do.getForms().stream()
                                .filter(lexicalForm -> lexicalForm.getProperty(lexInfo.getProperty("tense"))
                                .contains(lexInfo.getPropertyValue("past")))
                                .filter(lexicalForm -> lexicalForm.getProperty(lexInfo.getProperty("number"))
                                .contains(lexInfo.getPropertyValue(numberStr)))
                                .findFirst()
                                .orElseThrow()
                                .getWrittenRep().value);
            }

           
            

            // opposite select variable
            SelectVariable oppositeSelectVariable = LexicalEntryUtil.getOppositeSelectVariable(this.lexicalEntryUtil.getSelectVariable());
            // get subjectType of this sentence's object
            SubjectType subjectType = this.lexicalEntryUtil.getSubjectType(oppositeSelectVariable, domainOrRangeType);
            String qWord = this.lexicalEntryUtil.getSubjectBySubjectType(subjectType, language, null); // Who / What


            for (String auxilariesVerb : auxilaries) {
                sentence = String.format(
                        "%s %s %s %s %s?",
                        qWord,
                        auxilariesVerb,
                        annotatedVerb.getWrittenRepValue(),
                        preposition,
                        bindingVariable
                );
                generatedSentences.add(sentence);
            }

           
            if (!this.lexicalEntryUtil.hasInvalidDeterminerToken(this.lexicalEntryUtil.getSelectVariable())) {
                for (PropertyValue number : numberList) {
                    String conditionLabel = this.lexicalEntryUtil.getReturnVariableConditionLabel(oppositeSelectVariable);
                    String determiner = this.lexicalEntryUtil.getSubjectBySubjectType(
                            SubjectType.INTERROGATIVE_DETERMINER,
                            language,
                            null
                    );
                    String determinerToken = getDeterminerTokenByNumber(number, conditionLabel, determiner);

                    for (String auxilariesVerb : auxilaries) {
                        sentence = String.format(
                                "%s %s %s %s %s?",
                                determinerToken,
                                auxilariesVerb,
                                annotatedVerb.getWrittenRepValue(),
                                preposition,
                                bindingVariable
                        );
                        generatedSentences.add(sentence);
                    }

                    
                }
            }
        }
        return generatedSentences;
    }

}
