package grammar.generator.helper.sentencetemplates;

import grammar.structure.component.DomainOrRangeType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.net.URI;

@Data
@Accessors(chain = true)
public class NP implements QueGGSentence {
  private Det determiner;
  private DomainOrRangeType domain;
  private URI range;
  private AnnotatedNoun noun;
  private AnnotatedPreposition preposition;
  // TODO: include adjectives?

  @Override
  public String getSentence() {
    return String.format("%s %s %s", determiner.getWrittenRepValue(), noun.getWrittenRepValue(), preposition.getWrittenRepValue());
  }
}
