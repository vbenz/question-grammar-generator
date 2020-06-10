package grammar.generator.helper.sentencetemplates;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnnotatedNoun extends AnnotatedWord {
  private URI gender;
  private URI number;
  private URI gCase;
}
