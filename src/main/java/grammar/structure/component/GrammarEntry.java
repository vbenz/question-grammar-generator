package grammar.structure.component;

import lombok.Data;
import org.apache.jena.query.QueryType;

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Data
public class GrammarEntry implements Serializable {
  private String id;
  private Language language;
  private SentenceType type;
  private DomainOrRangeType bindingType;
  private DomainOrRangeType returnType;
  private FrameType frameType;
  private List<String> sentences = new ArrayList<>();
  private QueryType queryType;
  private String sparqlQuery;
  private Map<String, String> sentenceToSparqlParameterMapping;
  private String returnVariable; // aka selectVariable
  private boolean isCombination = false;
  private SentenceBindings sentenceBindings;

  /**
   * This is a convenience method to retrieve the mapped binding variable from
   * {@link #sentenceToSparqlParameterMapping}.<br>
   * Make sure that the map is filled before trying to use this.
   *
   * @return the string binding variable name (e.g. subjOfProp)
   */
  @Transient
  public String getBindingVariable() {
    assert this.getSentenceToSparqlParameterMapping() != null;
    return this.getSentenceToSparqlParameterMapping()
               .get(sentenceBindings.getBindingVariableName());
  }

  @Transient
  public GrammarEntry deepCopy() {
    GrammarEntry grammarEntry = new GrammarEntry();
    grammarEntry.id = this.id;
    grammarEntry.language = this.language;
    grammarEntry.type = this.type;
    grammarEntry.bindingType = this.bindingType;
    grammarEntry.returnType = this.returnType;
    grammarEntry.frameType = this.frameType;
    grammarEntry.sentences.addAll(this.sentences);
    grammarEntry.queryType = this.queryType;
    grammarEntry.sparqlQuery = this.sparqlQuery;
    grammarEntry.returnVariable = this.returnVariable;
    grammarEntry.isCombination = this.isCombination;
    if (this.sentenceToSparqlParameterMapping != null) {
      grammarEntry.sentenceToSparqlParameterMapping = new HashMap<>(this.sentenceToSparqlParameterMapping);
    }
    if (this.sentenceBindings != null) {
      grammarEntry.sentenceBindings = this.sentenceBindings.deepCopy();
    }
    return grammarEntry;
  }

  public String toString() {
    return "\n" +
      "GrammarEntry(\n" +
      "  type =\t" + this.getType() + "\n" +
      "  bindingType =\t" + this.getBindingType() + "\n" +
      "  returnType =\t" + this.getReturnType() + "\n" +
      "  frameType =\t" + this.getFrameType() + ",\n" +
      "  S =\t" + this.getSentences() + ",\n" +
      "  queryType =\t" + this.getQueryType() + ",\n" +
      "-".repeat(30) + "\n" +
      "sparqlQuery =\n" +
      this.getSparqlQuery() + ",\n" +
      "-".repeat(30) + "\n" +
      "  sentenceToSparqlParameterMapping = \t" + this.getSentenceToSparqlParameterMapping() + ",\n" +
      "  returnVariable =\t" + this.getReturnVariable() + ",\n" +
      "  isCombination =\t" + this.isCombination() + ",\n" +
      "  bindings =\n" +
      (!isNull(this.getSentenceBindings()) ? this.getSentenceBindings().toString() : Collections.EMPTY_LIST) + "\n" +
      ")";
  }
}
