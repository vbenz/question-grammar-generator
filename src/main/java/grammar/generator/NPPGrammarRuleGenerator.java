package grammar.generator;

import eu.monnetproject.lemon.impl.LexicalEntryImpl;
import eu.monnetproject.lemon.model.Frame;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalSense;
import eu.monnetproject.lemon.model.Property;
import eu.monnetproject.lemon.model.PropertyValue;
import eu.monnetproject.lemon.model.SynArg;
import eu.monnetproject.lemon.model.SyntacticRoleMarker;
import grammar.generator.helper.BindingConstants;
import grammar.generator.helper.SubjectType;
import grammar.generator.helper.sentencetemplates.AnnotatedPreposition;
import grammar.generator.helper.sentencetemplates.QueGGNPPSentence;
import grammar.generator.helper.sentencetemplates.QueGGNPPSentenceImpl;
import grammar.sparql.querycomponent.SelectVariable;
import grammar.structure.component.FrameType;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.GrammarEntryType;
import grammar.structure.component.Language;
import net.lexinfo.LexInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

public class NPPGrammarRuleGenerator extends GrammarRuleGeneratorRoot {

  public NPPGrammarRuleGenerator(Language language) {
    super(FrameType.NPP, language, BindingConstants.DEFAULT_BINDING_VARIABLE);
  }

  @Override
  public List<String> generateSentences(SelectVariable selectVariable, LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    List<String> generatedSentences = new ArrayList<>();
    String preposition = getPreposition(lexicalEntry);

    SubjectType subjectType = getSubjectType(selectVariable, getReferenceUri(lexicalEntry));

    QueGGNPPSentence queGGNPPSentence = new QueGGNPPSentenceImpl(getLexicon(), lexicalEntry, lexicalSense, subjectType);
    queGGNPPSentence.setObject(getBindingVariable());
    queGGNPPSentence.setNp(queGGNPPSentence.getNp().setPreposition((AnnotatedPreposition) new AnnotatedPreposition().setWrittenRepValue(preposition)));
    generatedSentences.add(queGGNPPSentence.getSentence());
    return generatedSentences;
  }

  @Override
  public GrammarEntry generateFragmentEntry(GrammarEntry grammarEntry, LexicalEntry lexicalEntry, LexicalSense lexicalSense) {
    GrammarEntry fragmentEntry = new GrammarEntry();

    fragmentEntry.setId(UUID.randomUUID().toString());
    fragmentEntry.setType(GrammarEntryType.NP);
    fragmentEntry.setDomainType(grammarEntry.getDomainType());
    fragmentEntry.setRangeType(grammarEntry.getRangeType());
    fragmentEntry.setLanguage(grammarEntry.getLanguage());
    fragmentEntry.setFrameType(grammarEntry.getFrameType());
    fragmentEntry.setBindings(grammarEntry.getBindings());
    fragmentEntry.setSparqlQuery(grammarEntry.getSparqlQuery());
    fragmentEntry.setReturnVariable(grammarEntry.getReturnVariable());
    fragmentEntry.setSparqlParameterMapping(grammarEntry.getSparqlParameterMapping());


    List<String> generatedSentences = new ArrayList<>();

    String preposition = getPreposition(lexicalEntry);
    QueGGNPPSentence queGGNPPSentence = new QueGGNPPSentenceImpl(getLexicon(), lexicalEntry, lexicalSense);
    queGGNPPSentence.setObject(getBindingVariable());
    queGGNPPSentence.setNp(queGGNPPSentence.getNp().setPreposition((AnnotatedPreposition) new AnnotatedPreposition().setWrittenRepValue(preposition)));
    generatedSentences.add(queGGNPPSentence.getNPSentence());

    fragmentEntry.setSentences(generatedSentences);

    return fragmentEntry;
  }


  private String getPreposition(LexicalEntry lexicalEntry) {
    String preposition = "";
    LexInfo lexInfo = new LexInfo();
    SynArg prepositionalAdjunct = lexInfo.getSynArg("prepositionalAdjunct");
    Property POS = lexInfo.getProperty("partOfSpeech");
    PropertyValue POSPreposition = lexInfo.getPropertyValue("preposition");
    Frame frame = getFrameByGrammarType(lexicalEntry);
    if (!isNull(frame)) {
      SyntacticRoleMarker synRoleMarker = frame.getSynArg(prepositionalAdjunct).iterator().next().getMarker();
      PropertyValue POSValue = synRoleMarker.getProperty(POS).iterator().next();
      if (POSValue.equals(POSPreposition)) {
        preposition = ((LexicalEntryImpl) synRoleMarker).getCanonicalForm().getWrittenRep().value;
      }
    }
    return preposition;
  }
}
