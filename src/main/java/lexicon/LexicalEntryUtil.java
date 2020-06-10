package lexicon;

import eu.monnetproject.lemon.model.Condition;
import eu.monnetproject.lemon.model.LexicalSense;

import java.net.URI;

public class LexicalEntryUtil {
  /**
   * Returns the specified condition from a lexical sense.
   * Will return an empty URI if there is no condition for this lexical sense.
   * @param lexicalSense the lexical sense that might hold a condition
   * @param condition the {@link Condition} predicate that should be retrieved
   * @return The URI of a condition domain or range / an empty URI if there is no condition
   */
  public static URI getConditionFromSense(LexicalSense lexicalSense, Condition condition) {
    return lexicalSense.getCondition(Condition.condition).iterator().hasNext() ?
        (URI) lexicalSense.getCondition(Condition.condition).iterator().next().getAnnotations(condition.getURI()).iterator().next() :
        URI.create("");
  }
}
