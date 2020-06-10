package grammar.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.monnetproject.lemon.model.Condition;
import eu.monnetproject.lemon.model.Frame;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import grammar.generator.helper.BindingConstants;
import grammar.generator.helper.PersonProperties;
import grammar.generator.helper.SubjectType;
import grammar.sparql.SPARQLRequest;
import grammar.sparql.querycomponent.Prefix;
import grammar.sparql.querycomponent.SelectVariable;
import grammar.structure.component.Binding;
import grammar.structure.component.DomainOrRangeType;
import grammar.structure.component.FrameType;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.GrammarEntryType;
import grammar.structure.component.GrammarWrapper;
import grammar.structure.component.Language;
import grammar.structure.component.VariableBindings;
import lexicon.LexicalEntryUtil;
import net.lexinfo.LexInfo;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.exceptions.QGGMissingFieldDeclarationException;
import util.matcher.PatternMatchHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grammar.sparql.SPARQLRequest.SPARQL_ENDPOINT_URL;
import static java.util.Objects.isNull;

public abstract class GrammarRuleGeneratorRoot implements GrammarRuleGenerator {
  private static final Logger LOG = LogManager.getLogger(GrammarRuleGeneratorRoot.class);

  private final FrameType frameType;
  private final Language language;
  private final String bindingVariable;
  private boolean searchOppositePropertyForBindings;
  private boolean addGenericTriple;
  private Lexicon lexicon;

  public GrammarRuleGeneratorRoot(FrameType frameType, Language language, String bindingVariable) {
    this.frameType = frameType;
    this.language = language;
    this.bindingVariable = bindingVariable;
    this.lexicon = null;
    this.addGenericTriple = true;
    this.searchOppositePropertyForBindings = true;
  }

  public FrameType getFrameType() {
    return frameType;
  }

  public Language getLanguage() {
    return language;
  }

  /**
   * Generates the SPARQL query that will later be saved as part of the grammar rule. <br>
   * For a sentence like "What is the capital of $x?": <br>
   * {@code
   * select distinct ?y where {
   * ?x dbo:capital ?y .
   * }
   * }
   *
   * @param selectVariable the variable that will actually be selected inside of the select scope of the SPARQL query.
   * @param lexicalEntry   the current lexical entry
   * @param lexicalSense   the lexical sense of the current lexical entry
   * @return a SPARQLRequest that contains a full SPARQL query string.
   */
  @Override
  public SPARQLRequest generateSPARQL(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense) throws QGGMissingFieldDeclarationException {
    return createLabelAndUriForPropertyRequest(selectVariable, getReferenceUri(lexicalEntry));
  }

  /**
   * Generates a list of sentences that will later be saved as part of the grammar rule.
   * For example: ["What is the capital of $x?"]
   *
   * @return a list of possible sentences
   */
  @Override
  public abstract List<String> generateSentences(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense) throws QGGMissingFieldDeclarationException;

  /**
   * Generates the list of bindings that will later be saved as part of the grammar rule.
   * This list will have following format if bindingVariable is $x and the sentence is "What is the capital of $x?":
   * <p>
   * ```
   * {
   * "$x":
   * [
   * ["Switzerland", "http://dbpedia.org/resource/Switzerland"],
   * ["Germany", "http://dbpedia.org/resource/Germany"],
   * ...
   * ]
   * }
   * ```
   * The first token (i.e. "Switzerland") is the label and the second one is the URI of the property.
   * To actually fill the list, a SPARQL query needs to be generated and executed.
   */
  @Override
  public List<Binding> generateBindings(SPARQLRequest sparqlRequest) throws QGGMissingFieldDeclarationException {
    SPARQLRequest sparqlRequestForBindings = getBindingSparqlRequest(sparqlRequest, true);
    ResultSet results = sparqlRequestForBindings.executeSelectQuery();

    return makeBindingsFromSPARQLRequestResult(sparqlRequestForBindings, results);
  }

  public SPARQLRequest getBindingSparqlRequest(SPARQLRequest sparqlRequest, boolean addLimit) throws QGGMissingFieldDeclarationException {
    SPARQLRequest sparqlRequestForBindings = new SPARQLRequest();

    if (searchOppositePropertyForBindings) {
      sparqlRequestForBindings.setSelectVariable(getOppositeSelectVariable(sparqlRequest.getSelectVariable()));
      sparqlRequestForBindings.setSearchProperty(sparqlRequest.getSearchProperty());
    } else {
      sparqlRequestForBindings = sparqlRequest;
    }
    if (addGenericTriple) {
      sparqlRequestForBindings.addGenericTriple();
    }
    sparqlRequestForBindings.addLabelQueryWithFilter(language);
    if (addLimit) {
      sparqlRequestForBindings.addLimit(10);
    }
    sparqlRequestForBindings.createQuery();
    return sparqlRequestForBindings;
  }

  /**
   * Writes the bindingVariable into the grammar rule.
   * The bindingValue is the variable that will be used for bindings in a sentence and for the bindings list (i.e. "$x").
   *
   * @return the bindingValue as String.
   */
  public String getBindingVariable() {
    return bindingVariable;
  }

  /**
   * Dumps sentence, SPARQL query, bindings, bindingValue, returnValue to a JSON file
   * with file name <code>fileName</code>.
   *
   * @param fileName       a file name (without the .json format ending).
   * @param grammarWrapper A helper class that holds List<GrammarEntry>
   */
  @Override
  public void dumpToJSON(String fileName, GrammarWrapper grammarWrapper) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValue(new File(fileName), grammarWrapper);
  }

  /**
   * Returns the whole grammar rule object for this GrammarRuleGenerator.
   *
   * @return a list of GrammarEntries for the specified lexicon, based on GrammarRuleGenerator's language.
   */
  @Override
  public List<GrammarEntry> generate(Lexicon lexicon) {
    List<GrammarEntry> grammarEntries = new ArrayList<>();

    // filter lexicon for correct grammar type:
    List<LexicalEntry> entriesFilteredByLanguageAndGrammarType = getEntriesFilteredByGrammarType(lexicon);
    try {

      for (LexicalEntry lexicalEntry : entriesFilteredByLanguageAndGrammarType) {

        for (LexicalSense lexicalSense : lexicalEntry.getSenses()) {
          SelectVariable selectVariable = getSelectVariable(lexicalEntry, lexicalSense);
          GrammarEntry grammarEntry = new GrammarEntry();
          grammarEntry.setId(UUID.randomUUID().toString());
          grammarEntry.setFrameType(frameType);
          grammarEntry.setLanguage(getLanguage());

          // generate SPARQL query
          SPARQLRequest sparqlRequest = generateSPARQL(selectVariable, lexicalEntry, lexicalSense);
          grammarEntry.setSparqlQuery(sparqlRequest.toString());

          // set return variable
          grammarEntry.setReturnVariable(getReturnVariable(sparqlRequest));

          // generate bindings for result sentence
          VariableBindings variableBindings = new VariableBindings();
          variableBindings.setBindingVariableName(getBindingVariable()); // maybe retrieve from sentence generation

          Map<String, String> sparqlParameterMapping = new HashMap<>();
          sparqlParameterMapping.put(this.bindingVariable, getOppositeSelectVariable(selectVariable).getVariableName());
          grammarEntry.setSparqlParameterMapping(sparqlParameterMapping);

          grammarEntry.setBindings(variableBindings);

          // generate sentences
          List<String> sentences = generateSentences(selectVariable, lexicalEntry, lexicalSense);
          grammarEntry.setSentences(sentences);

          DomainOrRangeType domainType = determineDomainOrRangeType(grammarEntry, lexicalSense, Condition.propertyDomain);
          DomainOrRangeType rangeType = determineDomainOrRangeType(grammarEntry, lexicalSense, Condition.propertyRange);
          grammarEntry.setDomainType(domainType);
          grammarEntry.setRangeType(rangeType);

          grammarEntry.setType(GrammarEntryType.SENTENCE);

          // generate a fragment, i.e. a single NP
          GrammarEntry fragmentEntry = generateFragmentEntry(grammarEntry, lexicalEntry, lexicalSense);

          grammarEntries.add(grammarEntry);
          if (!isNull(fragmentEntry)) {
            grammarEntries.add(fragmentEntry);
          }
        }
      }
    } catch (QGGMissingFieldDeclarationException e) {
      System.err.println(e.getMessage());
    }
    List<GrammarEntry> combinations = generateCombinations(grammarEntries);
    grammarEntries.addAll(combinations);
    return grammarEntries;
  }

  protected DomainOrRangeType determineDomainOrRangeType(GrammarEntry grammarEntry, LexicalSense lexicalSense, Condition condition) {
    URI domainUri = LexicalEntryUtil.getConditionFromSense(lexicalSense, condition);
    return DomainOrRangeType.getMatchingType(domainUri);
  }

  @Override
  public GrammarEntry generateFragmentEntry(GrammarEntry grammarEntry, LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    return null;
  }

  SubjectType getSubjectType(SelectVariable selectVariable, String uri) {
    String domainOrRange = mapDomainOrRange(selectVariable);
    return detectSubjectType(uri, domainOrRange);
  }

  SelectVariable getSelectVariable(LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    SelectVariable selectVariable;
    // URI value of the subject syn arg
    URI argValue = getFrameByGrammarType(lexicalEntry)
      .getSynArg(frameType.getSubjectEquivalentSynArg())
      .iterator().next().getURI();
    // match to sense arg value
    if (lexicalSense.getSubjOfProps().stream().anyMatch(argument -> argument.getURI().equals(argValue))) {
      selectVariable = SelectVariable.SUBJECT_OF_PROPERTY;
    } else if (lexicalSense.getObjOfProps().stream().anyMatch(argument -> argument.getURI().equals(argValue))) {
      selectVariable = SelectVariable.OBJECT_OF_PROPERTY;
    } else {
      LOG.error("No selectVariable found for {}, defaulting to {}",
        lexicalEntry.getURI(), SelectVariable.OBJECT_OF_PROPERTY.getVariableName());
      selectVariable = SelectVariable.OBJECT_OF_PROPERTY;
    }
    return selectVariable;
  }

  String mapDomainOrRange(SelectVariable selectVariable) {
    return selectVariable.equals(SelectVariable.SUBJECT_OF_PROPERTY) ? "domain" : "range";
  }

  private List<GrammarEntry> generateCombinations(List<GrammarEntry> grammarEntries) {
    final String newMappingForBindingVariable = "x";
    List<GrammarEntry> combinations = new ArrayList<>();
    List<GrammarEntry> sentenceGrammarEntries = grammarEntries.stream()
      .filter(grammarEntry -> grammarEntry.getType().equals(GrammarEntryType.SENTENCE))
      .collect(Collectors.toList());

    for (GrammarEntry sentenceEntry : sentenceGrammarEntries) {
      String targetType = parseFirstSentence(sentenceEntry.getSentences().get(0));
      GrammarEntryType grammarEntryType = GrammarEntryType.getMatchingType(targetType);
      List<GrammarEntry> filteredEntries = grammarEntries.stream()
        .filter(grammarEntry -> grammarEntry.getType().equals(grammarEntryType))
        .filter(grammarEntry -> grammarEntry.getRangeType().equals(sentenceEntry.getDomainType()))
        .collect(Collectors.toList());

      for (GrammarEntry filteredEntry : filteredEntries) {
        GrammarEntry combinedEntry = new GrammarEntry();
        for (String sentence : sentenceEntry.getSentences()) {
          for (String npSentence :  filteredEntry.getSentences()) {

            String combinedSentence = combineSentences(
              sentence,
              npSentence,
              sentenceEntry.getBindings().getBindingVariableName());
            combinedEntry.getSentences().add(combinedSentence);
          }
        }
        String newQueryPattern = combineSPARQL(
          sentenceEntry.getSparqlQuery(),
          getMappedSparqlBinding(sentenceEntry.getSparqlParameterMapping(), sentenceEntry.getBindings().getBindingVariableName()),
          filteredEntry.getSparqlQuery(),
          filteredEntry.getReturnVariable(),
          newMappingForBindingVariable
        );

        // Copy needed entries from base entry (sentenceEntry)
        combinedEntry.setReturnVariable(sentenceEntry.getReturnVariable());
        VariableBindings combinedBindings = new VariableBindings();
        combinedBindings.setBindingVariableName(sentenceEntry.getBindings().getBindingVariableName());
        combinedEntry.setBindings(combinedBindings);
        combinedEntry.setLanguage(sentenceEntry.getLanguage());
        combinedEntry.setType(sentenceEntry.getType());
        combinedEntry.setFrameType(sentenceEntry.getFrameType());
        combinedEntry.setDomainType(sentenceEntry.getDomainType());

        // Copy range from filteredEntry
        combinedEntry.setRangeType(filteredEntry.getRangeType());

        // Set new values for sentenceEntry
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put(sentenceEntry.getBindings().getBindingVariableName(), newMappingForBindingVariable);
        combinedEntry.setSparqlParameterMapping(combinedMap);
        combinedEntry.setSparqlQuery(newQueryPattern);

        // generate new id
        combinedEntry.setId(UUID.randomUUID().toString());

        combinations.add(combinedEntry);
      }
    }
    return combinations;
  }

  /**
   * Returns the value of the sparqlParameterMapping.
   *
   * @return the value of the sparqlParameterMapping i.e. "objOfProp"
   */
  private String getMappedSparqlBinding(Map<String, String> sparqlParameterMapping, String baseBindingVariable) {
    return sparqlParameterMapping.get(baseBindingVariable);
  }

  /**
   * Combines two basic graph patterns.
   *
   * @param baseSparql                   <p>The basic graph pattern that is used as base pattern for this query.
   *                                     This queries' returnVariable will be used for the resulting SELECT query.</p>
   * @param baseMappedSparqlParameter    <p>The name of the subject or object of the baseSparql query
   *                                     that is used for matching the matchedSparql.</p>
   * @param matchedSparql                <p>The basic graph pattern that will be joined with the baseSparql.</p>
   * @param matchedSparqlReturnVariable  <p>The returnVariable of the matchedSparql query</p>
   * @param newMappingForBindingVariable <p>The new SPARQL variable that will be the mapped to the bindingVariable</p>
   * @return the new SPARQL parameter that will be mapped by the bindingVariable of the baseSparql
   */
  private String combineSPARQL(
    String baseSparql,
    String baseMappedSparqlParameter,
    String matchedSparql,
    String matchedSparqlReturnVariable,
    String newMappingForBindingVariable) {


    Triple baseTriple = SSE.parseTriple(baseSparql);
    Triple matchedTriple = SSE.parseTriple(matchedSparql);

    // Check which variable needs to be renamed and will be the new binding of the base sparql query
    if (matchedTriple.getSubject().getName().equals(matchedSparqlReturnVariable)) {
      // subject matched to matchedSparqlReturnVariable
      //  -> subject needs to be renamed to baseMappedSparqlParameter
      //  -> object needs to be renamed to newMappingForBindingVariable
      matchedTriple = Triple.create(
        Var.alloc(baseMappedSparqlParameter),
        matchedTriple.getPredicate(),
        Var.alloc(newMappingForBindingVariable)
      );
    } else {
      // object matched to matchedSparqlReturnVariable
      //  -> subject needs to be renamed to newMappingForBindingVariable
      //  -> object needs to be renamed to baseMappedSparqlParameter
      matchedTriple = Triple.create(
        Var.alloc(newMappingForBindingVariable),
        matchedTriple.getPredicate(),
        Var.alloc(baseMappedSparqlParameter)
      );
    }
    ElementTriplesBlock block = new ElementTriplesBlock();
    block.addTriple(baseTriple);
    block.addTriple(matchedTriple);
    //SSE.parseGraph("(graph" + block.getPattern().toString()+")").toString()
    return block.toString();
  }

  private String combineSentences(
    String baseSentence,
    String matchedSentence,
    String bindingVariable) {
    String combinedSentence = "";

    // Try to find the matching uri for the previously found item inside the QALD SPARQL query
    String matchPattern = String.format("\\(%s \\| PERSON_NP\\(%s\\)\\)", bindingVariable, bindingVariable);
    matchPattern = PatternMatchHelper.cleanPattern(matchPattern);
    Pattern pattern = Pattern.compile(matchPattern);
    String expressionToReplace = PatternMatchHelper.getPatternMatch(baseSentence, pattern, 0);
    if (!expressionToReplace.isEmpty()) {
      // Could not find a matching prefixed property inside the QALD SPARQL query
      combinedSentence = baseSentence.replaceAll(matchPattern, PatternMatchHelper.cleanPattern(matchedSentence));
    } else {
      System.out.println("No match");
    }
    return combinedSentence;
  }

  // TODO: actually parse
  private String parseFirstSentence(String s) {
    return "NP";
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
    List<String> mapsToWho = Stream.of(PersonProperties.values()).map(PersonProperties::getUri).collect(Collectors.toList());
    String domainOrRangeResponse = "";
    ParameterizedSparqlString parameterizedSparqlString = createSPARQLRequestForSubjectType(uri, domainOrRange);
    QueryExecution exec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URL, parameterizedSparqlString.asQuery());
    ResultSet resultSet = exec.execSelect();
    // check only first result as we are not interested in anything but Person
    QuerySolution querySolution;
    if (resultSet.hasNext()) {
      querySolution = resultSet.next();
    } else {
//      throw new NotFoundException(parameterizedSparqlString.toString());
      if (domainOrRange.equals("subClassOf")) {
        return SubjectType.THING;
      }
      return detectSubjectType(uri, "subClassOf");
    }
    if (!isNull(querySolution)) {
      domainOrRangeResponse = querySolution.get(domainOrRange).toString();
    }
    // always default to SubjectType.THING if not Person or not found
    // check for label instead of uri.... Q215627 is person as well...
    return mapsToWho.contains(domainOrRangeResponse) ? SubjectType.PERSON : SubjectType.THING;
  }

  private ParameterizedSparqlString createSPARQLRequestForSubjectType(String lexicalEntryRef, String domainOrRange) {
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

  private SelectVariable getOppositeSelectVariable(SelectVariable selectVariable) {
    return selectVariable
      .equals(SelectVariable.SUBJECT_OF_PROPERTY)
      ? SelectVariable.OBJECT_OF_PROPERTY
      : SelectVariable.SUBJECT_OF_PROPERTY;
  }

  String getReferenceUri(LexicalEntry lexicalEntry) {
    return lexicalEntry.getSenses().iterator().next().getReference().toString();
  }

  /**
   * Writes the returnVariable into the grammar rule.
   * The returnVariable is the variable that will be selected in the SPARQL query (i.e. "?x").
   *
   * @return the returnVariable as String.
   */
  String getReturnVariable(SPARQLRequest sparqlRequest) {
    return sparqlRequest.getSelectVariable().getVariableName();
  }

  private SPARQLRequest createLabelAndUriForPropertyRequest(SelectVariable selectVariable, String searchProperty) throws QGGMissingFieldDeclarationException {
    SPARQLRequest sparqlRequest = new SPARQLRequest();

    sparqlRequest.setSelectVariable(selectVariable);
    sparqlRequest.setSearchProperty(searchProperty);
    sparqlRequest.addGenericTriple();
//    sparqlRequest.addLimit(10);
//    sparqlRequest.addLabelQueryWithFilter(language);
    sparqlRequest.createQuery();

    return sparqlRequest;
  }

  private List<Binding> makeBindingsFromSPARQLRequestResult(SPARQLRequest sparqlRequest, ResultSet resultSet) {
    List<Binding> bindingsList = new ArrayList<>();
    while (resultSet.hasNext()) {
      QuerySolution qs = resultSet.next();

      Binding binding = new Binding();
      String uri = sparqlRequest.getSelectVariable().getVariableName(); // i.e. "objOfProp"

      try {
        if (qs.getLiteral(BindingConstants.LABEL) != null) {
          binding.setLabel(qs.getLiteral(BindingConstants.LABEL).getLexicalForm());
          binding.setUri(qs.get(uri).asResource().getURI());
        } else if (qs.get(this.getReturnVariable(sparqlRequest)) != null) {
          binding.setLabel(qs.get("?x").asResource().getLocalName());
          binding.setUri(qs.get("?x").asResource().getURI());
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
      bindingsList.add(binding);
    }
    return bindingsList;
  }

  private Frame getFrameForLexicalEntryAndFrameClass(LexicalEntry lexicalEntry, URI frameClass) {
    return lexicalEntry.getSynBehaviors()
      .stream()
      .filter(frameEntry -> frameEntry.getTypes().contains(frameClass))
      .findFirst()
      .orElse(null);
  }

  /**
   * Returns the matching frame of the current grammar type
   * if the lexical entry declared the grammar type as syntactic behaviour.
   *
   * @param lexicalEntry The lexical entry that holds the frame
   * @return a frame object or null
   */
  Frame getFrameByGrammarType(LexicalEntry lexicalEntry) {
    URI frameClass = new LexInfo().getFrameClass(frameType.getName());
    return getFrameForLexicalEntryAndFrameClass(lexicalEntry, frameClass);
  }

  public List<LexicalEntry> getEntriesFilteredByGrammarType(Lexicon lexicon) {
    List<LexicalEntry> list = new ArrayList<>();
    for (LexicalEntry entry : lexicon.getEntrys()) {
      for (Frame frame : entry.getSynBehaviors()) {
        frame.getTypes().stream()
          // will probably still only be one entry!
          .filter(uri -> uri.toString().equals(Prefix.LEXINFO.getUri() + frameType.getName()))
          .forEach(uri -> list.add(entry));
      }
    }
    return list;
  }

  public boolean isSearchOppositePropertyForBindings() {
    return searchOppositePropertyForBindings;
  }

  public void setSearchOppositePropertyForBindings(boolean searchOppositePropertyForBindings) {
    this.searchOppositePropertyForBindings = searchOppositePropertyForBindings;
  }

  public void setAddGenericTriple(boolean addGenericTriple) {
    this.addGenericTriple = addGenericTriple;
  }

  public Lexicon getLexicon() {
    return lexicon;
  }

  public void setLexicon(Lexicon lexicon) {
    this.lexicon = lexicon;
  }
}
