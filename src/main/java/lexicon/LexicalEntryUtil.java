package lexicon;

import eu.monnetproject.lemon.impl.LexicalEntryImpl;
import eu.monnetproject.lemon.model.Condition;
import eu.monnetproject.lemon.model.Frame;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.PropertyValue;
import eu.monnetproject.lemon.model.SynArg;
import eu.monnetproject.lemon.model.SyntacticRoleMarker;
import grammar.generator.OWLRestriction;
import grammar.generator.helper.SubjectType;
import grammar.generator.helper.datasets.questionword.QuestionWordFactory;
import grammar.generator.helper.datasets.questionword.QuestionWordRepository;
import grammar.generator.helper.sentencetemplates.AnnotatedNoun;
import grammar.generator.helper.sentencetemplates.AnnotatedNounOrQuestionWord;
import grammar.generator.helper.sentencetemplates.AnnotatedVerb;
import grammar.sparql.Prefix;
import grammar.sparql.SPARQLRequest;
import grammar.sparql.SelectVariable;
import grammar.structure.component.DomainOrRangeType;
import grammar.structure.component.FrameType;
import grammar.structure.component.Language;
import lombok.Getter;
import net.lexinfo.LexInfo;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QueryType;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.exceptions.QueGGMissingFactoryClassException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static grammar.sparql.Prefix.DBPEDIA;
import static grammar.sparql.SPARQLRequest.SPARQL_ENDPOINT_URL;
import static java.util.Objects.isNull;

@Getter
public class LexicalEntryUtil {

  private static final Logger LOG = LogManager.getLogger(LexicalEntryUtil.class);

  private final Lexicon lexicon;
  private final LexicalEntry lexicalEntry;
  private final FrameType frameType;
  private final LexicalSense lexicalSense;
  private final LexInfo lexInfo;
  private final Language language;
  private final OWLRestriction owlRestriction;

  public LexicalEntryUtil(
    Lexicon lexicon,
    LexicalEntry lexicalEntry,
    FrameType frameType,
    LexicalSense lexicalSense
  ) {
    this.lexicon = lexicon;
    this.lexicalEntry = lexicalEntry;
    this.frameType = frameType;
    this.lexicalSense = lexicalSense;
    this.lexInfo = new LexInfo();
    this.language = Language.stringToLanguage(lexicon.getLanguage());
    this.owlRestriction = new OWLRestriction(lexicon, lexicalSense.getReference()).invoke();
  }

  public List<AnnotatedVerb> loadToBeVerbs() {
    LexicalEntry entry = new LexiconSearch(lexicon).getReferencedResource("component_be");
    return parseLexicalEntryToAnnotatedVerbs(entry.getOtherForms());
  }

  public static SelectVariable getOppositeSelectVariable(SelectVariable selectVariable) {
    return selectVariable
             .equals(SelectVariable.SUBJECT_OF_PROPERTY)
           ? SelectVariable.OBJECT_OF_PROPERTY
           : SelectVariable.SUBJECT_OF_PROPERTY;
  }

  /**
   * Get the string representation of the pronoun or determiner that is or is part of the subject for the generated sentence.
   *
   * @param subjectType                 the {@link SubjectType} of the current lexical entry and sense.
   * @param language                    the current language
   * @param annotatedNounOrQuestionWord <p>a noun or descendent class word that the output word should match to (by number, gender).<br> Can be {@code null} if the {@link SubjectType} is not a determiner or if there is no need to match the output word to any noun.</p>
   * @return the string representation of a {@link SubjectType} matching by language and (if provided) by a noun's gender and number.
   */
  public String getSubjectBySubjectType(
    SubjectType subjectType,
    Language language,
    AnnotatedNounOrQuestionWord annotatedNounOrQuestionWord
  ) throws QueGGMissingFactoryClassException {
    String sbjType = "";
    QuestionWordRepository questionWordRepository = new QuestionWordFactory(language).init();
    List<AnnotatedNounOrQuestionWord> questionWords;
    questionWords = questionWordRepository
      .findByLanguageAndSubjectType(language, subjectType);
    if (questionWords.size() != 1) {
      questionWords = questionWordRepository
        .findByLanguageAndSubjectTypeAndNumberAndGender(
          language,
          subjectType,
          lexInfo.getPropertyValue("singular"),
          lexInfo.getPropertyValue("commonGender")
        );
    }
    if (!isNull(annotatedNounOrQuestionWord)) {
      if (questionWords.size() != 1) {
        questionWords = questionWordRepository
          .findByLanguageAndSubjectTypeAndNumber(
            language,
            subjectType,
            annotatedNounOrQuestionWord.getNumber()
          );
      }
      if (questionWords.size() != 1) {
        questionWords = questionWordRepository
          .findByLanguageAndSubjectTypeAndNumberAndGender(
            language,
            subjectType,
            annotatedNounOrQuestionWord.getNumber(),
            annotatedNounOrQuestionWord.getGender()
          );
      }
    }
    if (questionWords.size() != 1) {
      LOG.error("Cannot find a matching subject in QuestionWordFactory({})", language);
    } else {
      sbjType = questionWords.get(0).getWrittenRepValue();
    }
    return sbjType;
  }

  /**
   * Get a determiner token like "Which city" or "Which cities".
   * If the provided toBeVerb is a plural form, the conditionLabel will be changed to plural.
   *
   * @param number         the number of the output noun
   * @param conditionLabel a noun for the determiner. E.g. "city"
   * @param determiner     a determiner string. E.g. "Which"
   * @return a string representing the combination of a determiner with a noun based on the number of a verb. E.g. "Which cities"
   */
  public static String getDeterminerTokenByNumber(
    PropertyValue number, String conditionLabel,
    String determiner
  ) {
    String determinerToken;
    if (number.equals(new LexInfo().getPropertyValue("plural"))) {
      conditionLabel = getPluralFormEn(conditionLabel);
    }
    determinerToken = compileDeterminerToken(conditionLabel, determiner);
    return determinerToken.trim();
  }

  private static String getPluralFormEn(String noun) {
    return noun.endsWith("y") ?
           noun.replaceAll("y$", "ies") :
           noun.endsWith("s") ?
           noun.concat("es") :
           noun.concat("s");
  }

  private static String compileDeterminerToken(String returnVariableConditionLabel, String determiner) {
    return String.format(
      "%s %s",
      determiner,
      returnVariableConditionLabel
    );
  }

  /**
   * Parse the full list of forms of the current lexical entry to a list of {@link AnnotatedNoun}s.
   * This grants simple access to the most important properties of a {@link LexicalForm} of a noun.
   *
   * @return A list of annotated nouns
   */
  public List<AnnotatedNounOrQuestionWord> parseLexicalEntryToAnnotatedAnnotatedNounOrQuestionWords() {
    return parseLexicalEntryToAnnotatedAnnotatedNounOrQuestionWords(this.lexicalEntry.getForms());
  }

  private List<AnnotatedNounOrQuestionWord> parseLexicalEntryToAnnotatedAnnotatedNounOrQuestionWords(Collection<LexicalForm> lexicalForms) {
    List<AnnotatedNounOrQuestionWord> annotatedNouns = new ArrayList<>();
    for (LexicalForm lexicalForm : lexicalForms) {
      AnnotatedNoun annotatedNoun =
        new AnnotatedNoun(
          lexicalForm.getWrittenRep().value,
          getPropertyValueOrDefaultFromLexicalForm("number", "singular", lexicalForm),
          language
        );
      annotatedNoun.setGender(getPropertyValueOrDefaultFromLexicalForm("gender", null, lexicalForm));
      annotatedNoun.setGrammaticalCase(getPropertyValueOrDefaultFromLexicalForm("case", null, lexicalForm));
      annotatedNouns.add(annotatedNoun);
    }
    return annotatedNouns;
  }

  /**
   * Parse the full list of forms of the current lexical entry to a list of {@link AnnotatedVerb}s.
   * This grants simple access to the most important properties of a {@link LexicalForm} of a verb.
   *
   * @return A list of annotated verbs
   */
  public List<AnnotatedVerb> parseLexicalEntryToAnnotatedVerbs() {
    return parseLexicalEntryToAnnotatedVerbs(this.lexicalEntry.getForms());
  }

  /**
   * Parse the provided list of forms of the current lexical entry to a list of {@link AnnotatedVerb}s.
   * This grants simple access to the most important properties of a {@link LexicalForm} of a verb.
   *
   * @param lexicalForms a list of {@link LexicalForm}s of a {@link LexicalEntry}
   * @return A list of annotated verbs
   */
  public List<AnnotatedVerb> parseLexicalEntryToAnnotatedVerbs(Collection<LexicalForm> lexicalForms) {
    List<AnnotatedVerb> annotatedVerbs = new ArrayList<>();
    for (LexicalForm lexicalForm : lexicalForms) {
      if (isNull(lexicalForm.getWrittenRep())) {
        LOG.error("{} - {} is missing a writtenRep", this.lexicalEntry, lexicalForm.getURI());
        continue;
      }
      AnnotatedVerb annotatedVerb =
        new AnnotatedVerb(
          lexicalForm.getWrittenRep().value,
          language,
          getPropertyValueOrDefaultFromLexicalForm("number", "singular", lexicalForm),
          getPropertyValueOrDefaultFromLexicalForm("person", "thirdPerson", lexicalForm),
          getPropertyValueOrDefaultFromLexicalForm("tense", "present", lexicalForm)
        );
      annotatedVerb.setVerbFormMood(getPropertyValueOrDefaultFromLexicalForm("verbFormMood", null, lexicalForm));
      annotatedVerb.setAspect(getPropertyValueOrDefaultFromLexicalForm("aspect", null, lexicalForm));
      annotatedVerbs.add(annotatedVerb);
    }
    return annotatedVerbs;
  }

  private PropertyValue getPropertyValueOrDefaultFromLexicalForm(
    String property,
    String defaultPropertyValue,
    LexicalForm lexicalForm
  ) {
    return lexicalForm.getProperty(lexInfo.getProperty(property))
                      .stream()
                      .findFirst()
                      .orElse(!isNull(defaultPropertyValue) ? lexInfo.getPropertyValue(defaultPropertyValue) : null);
  }

  /**
   * Returns the specified condition from a lexical sense.
   * Will return an empty URI if there is no condition for this lexical sense.
   *
   * @param condition the {@link Condition} predicate that should be retrieved
   * @return The URI of a condition domain or range / an empty URI if there is no condition
   */
  public URI getConditionFromSense(Condition condition) {
    return this.lexicalSense.getCondition(Condition.condition).iterator().hasNext() ?
           (URI) this.lexicalSense.getCondition(Condition.condition)
                                  .iterator()
                                  .next()
                                  .getAnnotations(condition.getURI())
                                  .iterator()
                                  .next() :
           URI.create("");
  }

  /**
   * Detects the SubjectType of the given LexicalEntry.
   * This enables the determination of the question word during the sentence generation.
   * A SPARQL query is compiled and executed to get the domain or range directly from the ontology.
   * The domainOrRange parameter of the SPARQL query is determined by the provided selectVariable.
   * If the query fails, the result will be based on the condition of the provided lexicalSenseRefUri.
   *
   * @param selectVariable the select variable for the queried subject (or object!)
   * @return The {@link SubjectType} of the provided lexicalSenseRefUri
   */
  public SubjectType getSubjectType(SelectVariable selectVariable) {
    String domainOrRange = selectVariable.mapDomainOrRange();
    return detectSubjectType(getReferenceUri(), domainOrRange);
  }

  /**
   * Detects the SubjectType of the given LexicalEntry.
   * This enables the determination of the qWord that is being used in the sentence.
   *
   * @param uri           the property as uri string.
   * @param domainOrRange a string "domain" or "range"
   * @return a SubjectType THING or PERSON based on the respective domain or range.
   */
  private SubjectType detectSubjectType(String uri, String domainOrRange) {
    List<String> mapsToWho = DomainOrRangeType.PERSON.getReferences().stream()
                                   .map(URI::toString)
                                   .collect(Collectors.toList());
    String domainOrRangeResponse = "";
    ParameterizedSparqlString parameterizedSparqlString = createSPARQLRequestForSubjectType(uri, domainOrRange);
    QueryExecution exec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URL, parameterizedSparqlString.asQuery());
    ResultSet resultSet = exec.execSelect();
    // check only first result as we are not interested in anything but Person
    QuerySolution querySolution;
    if (resultSet.hasNext()) {
      querySolution = resultSet.next();
      if (!isNull(querySolution)) {
        domainOrRangeResponse = querySolution.get(domainOrRange).toString();
      }
    } else {
      SelectVariable selectVariable = domainOrRange.equals("domain") ?
                                      SelectVariable.SUBJECT_OF_PROPERTY :
                                      SelectVariable.OBJECT_OF_PROPERTY;
      domainOrRangeResponse = getConditionUriBySelectVariable(selectVariable).toString();
    }
    // always default to SubjectType.THING if not Person or not found
    // check for label instead of uri.... Q215627 is person as well...
    return mapsToWho.contains(domainOrRangeResponse) ?
           SubjectType.PERSON_INTERROGATIVE_PRONOUN :
           SubjectType.THING_INTERROGATIVE_PRONOUN;
  }

  private static ParameterizedSparqlString createSPARQLRequestForSubjectType(
    String lexicalEntryRef,
    String domainOrRange
  ) {
    ParameterizedSparqlString sparqlString = new ParameterizedSparqlString();
    sparqlString.setNsPrefix("rdfs", Prefix.RDFS.getUri());
    sparqlString.setCommandText(
      String.format("select ?%s\n" +
                      "where {\n" +
                      "<%s> rdfs:%s ?%s .\n" +
                      "}",
                    domainOrRange,
                    lexicalEntryRef, domainOrRange, domainOrRange
      )
    );
    return sparqlString;
  }

  /**
   * Get the URI of the condition of the current lexical entry and sense based on the provided selectVariable.
   *
   * @param selectVariable The return variable of the current lexical entry.
   * @return The matching domain or range condition based on the provided selectVariable.
   */
  public URI getConditionUriBySelectVariable(SelectVariable selectVariable) {
    URI domainOrRangeUri;
    if (selectVariable.equals(SelectVariable.SUBJECT_OF_PROPERTY)) {
      domainOrRangeUri = getConditionFromSense(Condition.propertyDomain);
    } else {
      domainOrRangeUri = getConditionFromSense(Condition.propertyRange);
    }
    return domainOrRangeUri;
  }

  /**
   * Get the URI reference of the current lexical sense as string.
   *
   * @return The string representation of the current lexical sense's reference.
   */
  public String getReferenceUri() {
    return this.lexicalSense.getReference().toString();
  }

  /**
   * Get the label of the current selectVariable-dependent condition entity using a SPARQL query on the ontology.
   *
   * @param selectVariable The return variable of the current lexical entry.
   * @return The label of the matching condition entity as it is provided by the ontology.
   */
  public String getReturnVariableConditionLabel(
    SelectVariable selectVariable
  ) {
    URI domainOrRangeUri = getConditionUriBySelectVariable(selectVariable);

    SPARQLRequest sparqlRequest = new SPARQLRequest();
    sparqlRequest.setSelectVariable(selectVariable);
    sparqlRequest.setSearchProperty(domainOrRangeUri.toString());
    sparqlRequest.addLabelQueryWithFilter(language, domainOrRangeUri.toString());
    Query query = sparqlRequest.initQuery(QueryType.SELECT);
    query.addResultVar(RDFS.label.getLocalName());
    sparqlRequest.setParameterizedSparqlString(new ParameterizedSparqlString(query.toString()));

    List<Map<String, String>> sparqlSelectResultList = sparqlRequest.getSparqlSelectResultList();
    return !sparqlSelectResultList.isEmpty() ?
           sparqlSelectResultList.get(0).get(RDFS.label.getLocalName()) :
           "";
  }

  /**
   * Checks the validity of the domain or range condition of the current lexical entry and lexical sense.<br>
   * The validity mainly depends on the condition entity being a part of the {@link Prefix#DBPEDIA} scope.
   *
   * @param selectVariable the select variable for the condition
   * @return true if condition invalid, else false
   */
  public boolean hasInvalidDeterminerToken(SelectVariable selectVariable) {
    return !isValidConditionForDeterminerToken(
      getConditionUriBySelectVariable(
        selectVariable
      )
    );
  }

  private boolean isValidConditionForDeterminerToken(URI conditionUri) {
    return conditionUri.toString().contains(DBPEDIA.getUri());
  }

  /**
   * Get the return variable of the current lexical entry by matching the arguments of
   * the current syntactical frame and the current sense.<br>
   * The return variable is always the sense property that matches {@link FrameType#getSubjectEquivalentSynArg()}<br>
   * {@link SelectVariable#OBJECT_OF_PROPERTY} is set a default if the check fails,<br>
   * but it is recommended to watch out for this warning as it may result in a faulty SPARQL query.
   *
   * @return The return variable of the current lexical entry and sense
   */
  public SelectVariable getSelectVariable() {
    SelectVariable selectVariable;
    // URI value of the subject syn arg
    URI argValue = getFrameByGrammarType()
      .getSynArg(this.frameType.getSubjectEquivalentSynArg())
      .iterator().next().getURI();
    // match to sense arg value
    if (lexicalSense.getSubjOfProps().stream().anyMatch(argument -> argument.getURI().equals(argValue))) {
      selectVariable = SelectVariable.SUBJECT_OF_PROPERTY;
    } else if (lexicalSense.getObjOfProps().stream().anyMatch(argument -> argument.getURI().equals(argValue))) {
      selectVariable = SelectVariable.OBJECT_OF_PROPERTY;
    } else if (lexicalSense.getIsAs().stream().anyMatch(argument -> argument.getURI().equals(argValue))) {
      selectVariable = SelectVariable.IS_A;
    } else {
      LOG.warn("No selectVariable found for {}, defaulting to {}",
               lexicalEntry.getURI(), SelectVariable.OBJECT_OF_PROPERTY.getVariableName()
      );
      selectVariable = SelectVariable.OBJECT_OF_PROPERTY;
    }
    return selectVariable;
  }

  /**
   * Returns the matching frame of the current grammar type
   * if the lexical entry declared the grammar type as syntactic behaviour.
   *
   * @return a frame object or null
   */
  public Frame getFrameByGrammarType() {
    URI frameClass = lexInfo.getFrameClass(this.frameType.getName());
    return getFrameForLexicalEntryAndFrameClass(frameClass);
  }

  private Frame getFrameForLexicalEntryAndFrameClass(URI frameClass) {
    return this.lexicalEntry.getSynBehaviors()
                            .stream()
                            .filter(frameEntry -> frameEntry.getTypes().contains(frameClass))
                            .findFirst()
                            .orElse(null);
  }

  /**
   * Get the string representation of the preposition of the current lexical entry.
   * The method fails if:<br>
   * <ul>
   *   <li>the preposition is not linked to the syntactical argument of a frame</li>
   *   <li>the frame of the current grammar type does not have the prepositionalAdjunct property</li>
   *   <li>the partOfSpeech of the preposition is not "preposition"</li>
   *   <li>{@link #getFrameByGrammarType()} fails</li>
   * </ul>
   *
   * @return The preposition of the current frame's prepositionalAdjunct
   */
  public String getPreposition() {
    String preposition = "";
    SynArg prepositionalAdjunct = lexInfo.getSynArg("prepositionalAdjunct");
    Property POS = lexInfo.getProperty("partOfSpeech");
    PropertyValue POSPreposition = lexInfo.getPropertyValue("preposition");
    Frame frame = getFrameByGrammarType();
    if (!isNull(frame)) {
      SyntacticRoleMarker synRoleMarker = frame.getSynArg(prepositionalAdjunct).iterator().next().getMarker();
      PropertyValue POSValue = synRoleMarker.getProperty(POS).iterator().next();
      if (POSValue.equals(POSPreposition)) {
        preposition = ((LexicalEntryImpl) synRoleMarker).getCanonicalForm().getWrittenRep().value;
      }
    }
    return preposition;
  }
}
