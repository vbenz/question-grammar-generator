package grammar.structure.component;

import eu.monnetproject.lemon.model.SynArg;
import net.lexinfo.LexInfo;

public enum FrameType {
  NPP("NounPPFrame", new LexInfo().getSynArg("copulativeArg")),
  VP("TransitiveFrame", new LexInfo().getSynArg("subject")),
  AP("AdjectivePredicateFrame", new LexInfo().getSynArg("copulativeSubject")),
  AA("AdjectiveAttributiveFrame", new LexInfo().getSynArg("attributiveArg"));

  private final String name;
  private final SynArg selectVariableSynArg;


  FrameType(String name, SynArg subjectEquivalentSynArg) {
    this.name = name;
    this.selectVariableSynArg = subjectEquivalentSynArg;
  }

  public String getName() {
    return name;
  }

  public SynArg getSubjectEquivalentSynArg() {
    return selectVariableSynArg;
  }
}
