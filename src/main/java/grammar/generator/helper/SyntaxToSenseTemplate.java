package grammar.generator.helper;

import grammar.sparql.querycomponent.Prefix;
import grammar.sparql.querycomponent.SelectVariable;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SyntaxToSenseTemplate {
  private Map<String, String> syntaxToSenseMapDomain;
  private Map<String, String> syntaxToSenseMapRange;
  private Map<String, String> syntaxToSenseMapDomain2;
  private Map<String, String> syntaxToSenseMapRange2;

  public SyntaxToSenseTemplate() {
    this.syntaxToSenseMapDomain = createSyntaxToSenseMapDomain();
    this.syntaxToSenseMapRange = createSyntaxToSenseMapRange();
    this.syntaxToSenseMapDomain2 = createSyntaxToSenseMapDomain2();
    this.syntaxToSenseMapRange2 = createSyntaxToSenseMapRange2();
  }

  private Map<String, String> createSyntaxToSenseMapDomain() {
    Map<String, String>  syntaxToSenseMapDomain = new HashMap<>();
    syntaxToSenseMapDomain.put(Prefix.LEXINFO.getUri() + "subject", Prefix.LEMON.getUri() + SelectVariable.SUBJECT_OF_PROPERTY.getVariableName());
    syntaxToSenseMapDomain.put(Prefix.LEXINFO.getUri() + "prepositionalArg", Prefix.LEMON.getUri() + SelectVariable.OBJECT_OF_PROPERTY.getVariableName());
    return syntaxToSenseMapDomain;
  }

  private Map<String, String> createSyntaxToSenseMapRange() {
    Map<String, String>  syntaxToSenseMapRange = new HashMap<>();
    syntaxToSenseMapRange.put(Prefix.LEXINFO.getUri() + "subject", Prefix.LEMON.getUri() + SelectVariable.OBJECT_OF_PROPERTY.getVariableName());
    syntaxToSenseMapRange.put(Prefix.LEXINFO.getUri() + "prepositionalArg", Prefix.LEMON.getUri() + SelectVariable.SUBJECT_OF_PROPERTY.getVariableName());
    return syntaxToSenseMapRange;
  }

  private Map<String, String> createSyntaxToSenseMapDomain2() {
    Map<String, String>  syntaxToSenseMapDomain = new HashMap<>();
    syntaxToSenseMapDomain.put(Prefix.LEXINFO.getUri() + "subject", Prefix.LEMON.getUri() + SelectVariable.SUBJECT_OF_PROPERTY.getVariableName());
    syntaxToSenseMapDomain.put(Prefix.LEXINFO.getUri() + "directObject", Prefix.LEMON.getUri() + SelectVariable.OBJECT_OF_PROPERTY.getVariableName());
    return syntaxToSenseMapDomain;
  }
  private Map<String, String> createSyntaxToSenseMapRange2() {
    Map<String, String>  syntaxToSenseMapRange = new HashMap<>();
    syntaxToSenseMapRange.put(Prefix.LEXINFO.getUri() + "subject", Prefix.LEMON.getUri() + SelectVariable.OBJECT_OF_PROPERTY.getVariableName());
    syntaxToSenseMapRange.put(Prefix.LEXINFO.getUri() + "directObject", Prefix.LEMON.getUri() + SelectVariable.SUBJECT_OF_PROPERTY.getVariableName());
    return syntaxToSenseMapRange;
  }
}
