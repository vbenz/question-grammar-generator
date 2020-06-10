package grammar.generator.helper.sentencetemplates;

import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import grammar.generator.helper.SubjectType;

public interface QueGGNPPSentence extends QueGGSentence {
  /**
   * i.e. What
   */
  void setSubject(String copulativeArg);

  /**
   * i.e.is / are
   */
  void setCopula(String copula);

  /**
   * i.e. the capital of
   */
  void setNp(NP np);

  /**
   * Get a sentence like "the capital of $x"
   */
  String getNPSentence();

  /**
   * i.e. $x / Germany
   */
  void setObject(String prepositionalAdjunct);

  /**
   * i.e. the biggest country of europe
   * @param NP another Npp
   */
  void setObject(NP NP);

  /**
   * Parses a lexical entry and its sense.
   * @param lexicon the lexicon
   * @param lexicalEntry the lexical entry
   * @param lexicalSense the lexical sense
   * @param subjectType the subject type (who / what)
   */
  void parseLexicalEntry(Lexicon lexicon, LexicalEntry lexicalEntry, LexicalSense lexicalSense, SubjectType subjectType);

  NP getNp();
}
