package grammar.generator;

import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import grammar.sparql.SPARQLRequest;
import grammar.sparql.querycomponent.SelectVariable;
import grammar.structure.component.Binding;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.GrammarWrapper;
import util.exceptions.QGGMissingFieldDeclarationException;

import java.io.IOException;
import java.util.List;

public interface GrammarRuleGenerator {

  /**
   * Generates the SPARQL query that will later be saved as part of the grammar rule. <br>
   * For a sentence like "What is the capital of $x?": <br>
   * {@code
   * select distinct ?y where {
   * ?x dbo:capital ?y .
   * }
   * }
   *
   * @param lexicalEntry the current lexical entry
   * @param lexicalSense the lexical sense of the current lexical entry
   * @return a SPARQLRequest that contains a full SPARQL query string.
   */
  SPARQLRequest generateSPARQL(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense) throws QGGMissingFieldDeclarationException;

  /**
   * Generates the sentence that will later be saved as part of the grammar rule. <br>
   * For example: "What is the capital of $x?"
   *
   * @return a list of possible sentences.
   */
  List<String> generateSentences(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense) throws QGGMissingFieldDeclarationException;

  GrammarEntry generateFragmentEntry(GrammarEntry grammarEntry, LexicalEntry lexicalEntry, LexicalSense lexicalSense);

  /**
   * Generates the list of bindings that will later be saved as part of the grammar rule.<br>
   * This list will have following format if bindingVariable is $x and the sentence is "What is the capital of $x?":<br>
   * <p>
   * {@code
   * {
   * "$x":
   * [
   * {"Switzerland", "http://dbpedia.org/resource/Switzerland"},
   * {"Germany", "http://dbpedia.org/resource/Germany"},
   * ...
   * ]
   * }
   * }<br>
   * The first token (i.e. "Switzerland") is the label and the second one is the URI of the property.<br>
   * To actually fill the list, a SPARQL query needs to be generated and executed.<br>
   */
  List<Binding> generateBindings(SPARQLRequest sparqlRequest) throws QGGMissingFieldDeclarationException;

  /**
   * Writes the bindingVariable into the grammar rule.<br>
   * The bindingValue is the variable that will be used for bindings in a sentence and for the bindings list (i.e. "$x").<br>
   *
   * @return the bindingValue as String.
   */
  String getBindingVariable();

  /**
   * Dumps sentence, SPARQL query, bindings, bindingValue, returnValue to a JSON file<br>
   * with file name <b>fileName</b>.<br>
   *
   * @param fileName       a file name (without the .json format ending).
   * @param grammarWrapper the wrapper that holds the grammar entries
   */
  void dumpToJSON(String fileName, GrammarWrapper grammarWrapper) throws IOException;

  /**
   * Returns the whole grammar rule object.<br>
   * Needs to be used last after every other generation rule.<br>
   *
   * @return a complete grammar rule object list
   */
  List<GrammarEntry> generate(Lexicon lexicon) throws QGGMissingFieldDeclarationException;
}
