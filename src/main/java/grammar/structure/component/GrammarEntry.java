package grammar.structure.component;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Data
public class GrammarEntry implements Serializable {
  private String id;
  private Language language;
  private GrammarEntryType type;
  private DomainOrRangeType domainType;
  private DomainOrRangeType rangeType;
  private FrameType frameType;
  private List<String> sentences = new ArrayList<>();
  private String sparqlQuery;
  private Map<String, String> sparqlParameterMapping;
  private String returnVariable; // aka selectVariable
  private VariableBindings bindings;

  public String toString() {
    return "\n" +
        "GrammarEntry(\n" +
        "  type =\t" + this.getType() + "\n" +
        "  domainType =\t" + this.getDomainType() + "\n" +
        "  rangeType =\t" + this.getRangeType() + "\n" +
        "  frameType =\t" + this.getFrameType() + ",\n" +
        "  S =\t" + this.getSentences() + ",\n" +
        "-".repeat(30) + "\n" +
        "sparqlQuery =\n" +
        this.getSparqlQuery() + ",\n" +
        "-".repeat(30) + "\n" +
        "  sparqlParameterMapping = \t" + this.getSparqlParameterMapping() + ",\n" +
        "  returnVariable =\t" + this.getReturnVariable() + ",\n" +
        "  bindings =\n" +
        (!isNull(this.getBindings()) ? this.getBindings().toString() : Collections.EMPTY_LIST) + "\n" +
        ")";
  }
}
