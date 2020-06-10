package grammar.generator;

import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import grammar.generator.helper.BindingConstants;
import grammar.generator.helper.SentenceConstants;
import grammar.generator.helper.SubjectType;
import grammar.generator.helper.sentencetemplates.SentenceBuilderNominalPhraseEN;
import grammar.sparql.SPARQLRequest;
import grammar.sparql.querycomponent.SelectVariable;
import grammar.structure.component.Binding;
import grammar.structure.component.FrameType;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.Language;
import grammar.structure.component.VariableBindings;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.exceptions.QGGMissingFieldDeclarationException;
import util.logging.LoggerHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static grammar.sparql.SPARQLRequest.SPARQL_ENDPOINT_URL;

public class AdjectiveAttributiveGrammarRuleGenerator extends GrammarRuleGeneratorRoot {
  private static final Logger LOG = LogManager.getLogger(AdjectiveAttributiveGrammarRuleGenerator.class);

  public AdjectiveAttributiveGrammarRuleGenerator(Language language) {
    super(FrameType.AA, language, BindingConstants.NONE);
    setSearchOppositePropertyForBindings(false);
    setAddGenericTriple(false);
  }

  /**
   * Generates a list of sentences that will later be saved as part of the grammar rule.
   * For example: ["What is the capital of $x?"]
   *
   * @param selectVariable the current select variable for a lexical entry
   * @param lexicalEntry   the current lexical entry.
   * @param lexicalSense   currently not used
   * @return a list of possible sentences
   */
  public List<String> generateSentences(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense, Binding binding) {
    SentenceConstants sentenceConstants = new SentenceConstants();

    String argUri = binding.getLabel();
    SubjectType subjectType = getSubjectType(selectVariable, binding.getUri());
    String qWord = sentenceConstants.getLanguageQWordMap().get(getLanguage()).get(subjectType);

    SentenceBuilderNominalPhraseEN sentenceBuilderNominalPhraseEN = new SentenceBuilderNominalPhraseEN(qWord, lexicalEntry.getCanonicalForm().getWrittenRep().value, argUri);
    return List.of(sentenceBuilderNominalPhraseEN.getSentence(selectVariable, ""));
  }

  public SPARQLRequest generateSPARQL(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense, Binding binding) throws QGGMissingFieldDeclarationException {
    OWLRestriction owlRestriction = new OWLRestriction(getLexicon(), lexicalEntry, lexicalSense).invoke();
    String property = owlRestriction.getProperty();
    String value = owlRestriction.getValue();

    SPARQLRequest sparqlRequest = new SPARQLRequest();
    sparqlRequest.setSearchProperty(property);
    sparqlRequest.setSelectVariable(selectVariable);

    Triple adjTriple =
        Triple.create(
            Var.alloc(selectVariable.getVariableName()),
            NodeFactory.createURI(property),
            NodeFactory.createURI(value));
    sparqlRequest.addTriple(adjTriple);

    //-------------------------------------------
    String argUri = binding.getUri();
    // ---------------------------------------

    Triple argTriple =
        Triple.create(
            Var.alloc(selectVariable.getVariableName()),
            RDF.type.asNode(),
            NodeFactory.createURI(argUri));
    sparqlRequest.addTriple(argTriple);
//    sparqlRequest.addLimit(10);

    sparqlRequest.createQuery();

    return sparqlRequest;
  }

  @Override
  public List<String> generateSentences(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    OWLRestriction owlRestriction = new OWLRestriction(getLexicon(), lexicalEntry, lexicalSense).invoke();
    Binding binding = getMostFrequentClass(selectVariable, owlRestriction).get(0);

    return generateSentences(selectVariable, lexicalEntry, lexicalSense, binding);
  }

  @Override
  public List<GrammarEntry> generate(Lexicon lexicon) {
    List<GrammarEntry> grammarEntries = new ArrayList<>();

    // filter lexicon for correct grammar type:
    List<LexicalEntry> entriesFilteredByLanguageAndGrammarType = getEntriesFilteredByGrammarType(lexicon);

    try {
      for (LexicalEntry lexicalEntry : entriesFilteredByLanguageAndGrammarType) {
        for (LexicalSense lexicalSense : lexicalEntry.getSenses()) {
          OWLRestriction owlRestriction = new OWLRestriction(getLexicon(), lexicalEntry, lexicalSense).invoke();
          if (!lexicalSense.getReference().getHost().equals("localhost")) {
            LOG.error("Lexical {} entry does not have a local reference. The reference is {}", lexicalEntry.getURI(), lexicalSense.getURI());
          }
          SelectVariable selectVariable = getSelectVariable(lexicalEntry, lexicalSense);
          List<Binding> propertiesOfMostFrequentClasses = getMostFrequentClass(selectVariable, owlRestriction);
          propertiesOfMostFrequentClasses = filterOutSameLabels(propertiesOfMostFrequentClasses);
          for (Binding properties : propertiesOfMostFrequentClasses) {
            GrammarEntry grammarEntry = new GrammarEntry();
            grammarEntry.setFrameType(getFrameType());

            // generate SPARQL query
            SPARQLRequest sparqlRequest = generateSPARQL(selectVariable, lexicalEntry, lexicalSense, properties);
            grammarEntry.setSparqlQuery(sparqlRequest.toString());

            // set return variable
            grammarEntry.setReturnVariable(getReturnVariable(sparqlRequest));

            // generate bindings for result sentence
            VariableBindings variableBindings = new VariableBindings();
            variableBindings.setBindingVariableName(getBindingVariable()); // maybe retrieve from sentence generation

            grammarEntry.setBindings(variableBindings);

            // generate sentences
            List<String> sentences = generateSentences(selectVariable, lexicalEntry, lexicalSense, properties);
            grammarEntry.setSentences(sentences);
            grammarEntries.add(grammarEntry);
          }
        }
      }
    } catch (QGGMissingFieldDeclarationException e) {
      LOG.error("QGGMissingFieldDeclarationException: " + e.getMessage());
    }
    return grammarEntries;
  }

  /**
   * Filter bindings for same label, return only one binding per label
   */
  private List<Binding> filterOutSameLabels(List<Binding> propertiesOfMostFrequentClasses) {
    return new ArrayList<>(
        propertiesOfMostFrequentClasses
            .stream()
            .collect(
                Collectors.toMap(
                    Binding::getLabel,
                    Function.identity(),
                    BinaryOperator.minBy(Comparator.comparing(Binding::getUri))
                )
            ).values());
  }

  private List<Binding> getMostFrequentClass(SelectVariable selectVariable, OWLRestriction owlRestriction) {
//  sparqlRequest.block.getPattern().getList().get(1).getMatchObject()
    SPARQLRequest innerRequest = new SPARQLRequest();
    innerRequest.setSearchProperty(owlRestriction.getProperty());
    innerRequest.setSelectVariable(selectVariable);

    innerRequest.getParameterizedSparqlString().setCommandText(
        String.format(
            "SELECT ?y (count(?y) as ?f) ?%s WHERE\n" +
                "{\n" +
                "  ?x <%s> <%s> ;\n" +
                "  <%s> ?y .\n" +
                "  ?y  <%s> ?%s .\n" +
                "  FILTER ( lang(?%s)=\"%s\" ) .\n" +
                "}\n" +
                "group by ?y ?%s\n" +
                "having (count(?y) > 9)" +
                "order by desc(?f)\n",
            RDFS.label.getLocalName(),
            owlRestriction.getProperty(), owlRestriction.getValue(),
            RDF.type,
            RDFS.label, RDFS.label.getLocalName(),
            RDFS.label.getLocalName(), getLanguage().toString().toLowerCase(),
            RDFS.label.getLocalName())
    );
    QueryExecution exec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URL, innerRequest.getParameterizedSparqlString().asQuery());
    ResultSet resultSet = exec.execSelect();
    QuerySolution querySolution;
    List<Binding> bindings = new ArrayList<>();
    while (resultSet.hasNext()) {
      querySolution = resultSet.next();
      bindings.add(
          new Binding(
              querySolution.getLiteral(RDFS.label.getLocalName()).getLexicalForm(),
              querySolution.get("y").asResource().getURI()
          )
      );
    }
    if (bindings.isEmpty()) {
      LOG.error("No results for binding query\n{}", LoggerHelper.sepString(owlRestriction.getProperty(), innerRequest.getParameterizedSparqlString().toString()));
    }
    return bindings;
  }

}
