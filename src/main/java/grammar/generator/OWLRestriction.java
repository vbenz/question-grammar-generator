package grammar.generator;

import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import lexicon.LexiconSearch;
import org.apache.jena.vocabulary.OWL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

import static java.util.Objects.isNull;

public class OWLRestriction {
  private static final Logger LOG = LogManager.getLogger(OWLRestriction.class);

  private final Lexicon lexicon;
  private final LexicalEntry lexicalEntry;
  private final LexicalSense lexicalSense;
  private String property;
  private String value;

  public OWLRestriction(Lexicon lexicon, LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    this.lexicon = lexicon;
    this.lexicalEntry = lexicalEntry;
    this.lexicalSense = lexicalSense;
  }

  public String getProperty() {
    return property;
  }

  public String getValue() {
    return value;
  }

  public OWLRestriction invoke() {
    property = "";
    value = "";
    LexicalEntry lexicalEntryAdj = LexiconSearch.getReferencedResource(lexicon, lexicalSense.getReference());
    if (!isNull(lexicalEntryAdj)) {
      property = lexicalEntryAdj.getAnnotations().get(URI.create(OWL.onProperty.getURI())).iterator().next().toString();
      value = lexicalEntryAdj.getAnnotations().get(URI.create(OWL.hasValue.getURI())).iterator().next().toString();
    } else {
      LOG.error("Could not find lexical entry {}", lexicalSense.getReference());
    }
    return this;
  }

}
