package grammar.generator.helper.sentencetemplates;

import eu.monnetproject.lemon.model.Condition;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Lexicon;
import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.SynArg;
import grammar.generator.helper.SentenceConstants;
import grammar.generator.helper.SubjectType;
import grammar.structure.component.DomainOrRangeType;
import grammar.structure.component.GrammarEntryType;
import grammar.structure.component.Language;
import lexicon.LexicalEntryUtil;
import net.lexinfo.LexInfo;

import java.net.URI;

import static lexicon.LexiconSearch.getReferencedResource;

public class QueGGNPPSentenceImpl implements QueGGNPPSentence {
  private static final URI FRAME_CLASS = new LexInfo().getFrameClass("NounPPFrame");
  private static final SynArg PREPOSITIONAL_ADJUNCT = new LexInfo().getSynArg("prepositionalAdjunct");
  private static final Property POS = new LexInfo().getProperty("partOfSpeech");
  private final Language language = Language.EN;
  private String copulativeArg;
  private String copula;
  private NP np;
  private String prepositionalAdjunct;
  private NP objectNP;

  public QueGGNPPSentenceImpl(Lexicon lexicon, LexicalEntry lexicalEntry, LexicalSense lexicalSense, SubjectType subjectType) {
    this.np = new NP();
    parseLexicalEntry(lexicon, lexicalEntry, lexicalSense, subjectType);
  }

  public QueGGNPPSentenceImpl(Lexicon lexicon, LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    this.np = new NP();
    fillNP(lexicon, lexicalEntry, lexicalSense);
  }

  public void parseLexicalEntry(Lexicon lexicon, LexicalEntry lexicalEntry, LexicalSense lexicalSense, SubjectType subjectType) {
    SentenceConstants sentenceConstants = new SentenceConstants();
    fillNP(lexicon, lexicalEntry, lexicalSense);
    URI rangeUri = LexicalEntryUtil.getConditionFromSense(lexicalSense, Condition.propertyRange);
    if (!rangeUri.equals(URI.create(""))) {
      DomainOrRangeType rangeType = DomainOrRangeType.getMatchingType(rangeUri);
      SubjectType subjectType1 = SubjectType.getMatchingType(rangeType.name());
      this.copulativeArg = sentenceConstants.getLanguageQWordMap().get(language).get(subjectType1);
    } else {
      this.copulativeArg = sentenceConstants.getLanguageQWordMap().get(language).get(subjectType);
    }
    this.copula = "is";
  }

  protected void fillNP(Lexicon lexicon, LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    AnnotatedNoun nppNoun = new AnnotatedNoun();
    Det det = new Det();
    nppNoun.setWrittenRepValue(lexicalEntry.getCanonicalForm().getWrittenRep().value);
//    nppNoun.setNumber();
    this.np.setNoun(nppNoun);

    URI domainUri = LexicalEntryUtil.getConditionFromSense(lexicalSense, Condition.propertyDomain);
    this.np.setDomain(DomainOrRangeType.getMatchingType(domainUri));

    LexicalEntry detEntry = getReferencedResource(lexicon, "component_the");
    det.setWrittenRepValue(detEntry.getCanonicalForm().getWrittenRep().value);

    this.np.setDeterminer(det);
  }

  @Override
  public NP getNp() {
    return this.np;
  }

  public String getSentence() {
    String sentence;
//    if (!isNull(objectNpp)) {
//      sentence = String.format("%s %s %s %s %s", copulativeArg, copula, npp.getSentence(), objectNpp.getSentence(), prepositionalAdjunct);
//    } else {
//      sentence = String.format("%s %s %s %s", copulativeArg, copula, npp.getSentence(), prepositionalAdjunct);
//    }
    sentence = String.format("%s %s %s (%s | %s_%s(%s))",
        copulativeArg,
        copula,
        np.getSentence(),
        prepositionalAdjunct,
        np.getDomain().toString(),
        GrammarEntryType.NP.toString(),
        prepositionalAdjunct);
    return sentence;
  }

  @Override
  public String getNPSentence() {
    return String.format("%s %s", this.np.getSentence(), prepositionalAdjunct);
  }

  /**
   * i.e. What / Which city
   *
   * @param copulativeArg the copulative argument
   */
  @Override
  public void setSubject(String copulativeArg) {
    this.copulativeArg = copulativeArg;
  }

  /**
   * i.e.is / are
   *
   * @param copula the copula
   */
  @Override
  public void setCopula(String copula) {
    this.copula = copula;
  }

  /**
   * i.e. the capital of
   *
   * @param np the lemma npp
   */
  @Override
  public void setNp(NP np) {
    this.np = np;
  }

  /**
   * i.e. $x / Germany
   *
   * @param prepositionalAdjunct the prepositional adjunct
   */
  @Override
  public void setObject(String prepositionalAdjunct) {
    this.prepositionalAdjunct = prepositionalAdjunct;
  }

  /**
   * i.e. the biggest country of europe
   *
   * @param NP another Npp
   */
  @Override
  public void setObject(NP NP) {
    this.objectNP = NP;
  }
}
