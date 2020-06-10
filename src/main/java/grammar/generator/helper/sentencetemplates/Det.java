package grammar.generator.helper.sentencetemplates;

import eu.monnetproject.lemon.model.PropertyValue;
import net.lexinfo.LexInfo;

public class Det extends AnnotatedNoun {
  private PropertyValue POSTag;
  Det() {
    this.POSTag = new LexInfo().getPropertyValue("noun");
  }
}
