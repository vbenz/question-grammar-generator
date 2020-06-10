package grammar.generator.helper.sentencetemplates;

import eu.monnetproject.lemon.model.PropertyValue;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AnnotatedWord {
  private PropertyValue POSTag;
  private boolean isCanonicalForm;
  private String writtenRepValue;
}
