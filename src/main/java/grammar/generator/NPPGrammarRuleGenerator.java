package grammar.generator;

import grammar.generator.helper.BindingConstants;
import grammar.generator.helper.SentenceBuilder;
import grammar.generator.helper.SentenceBuilderCopulativePP;
import grammar.structure.component.FrameType;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.Language;
import grammar.structure.component.SentenceType;
import lexicon.LexicalEntryUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.exceptions.QueGGMissingFactoryClassException;

import java.util.List;
import java.util.stream.Collectors;

public class NPPGrammarRuleGenerator extends GrammarRuleGeneratorRoot {
  private static final Logger LOG = LogManager.getLogger(NPPGrammarRuleGenerator.class);

  public NPPGrammarRuleGenerator(Language language) {
    super(FrameType.NPP, language, BindingConstants.DEFAULT_BINDING_VARIABLE);
  }

  @Override
  public List<String> generateSentences(LexicalEntryUtil lexicalEntryUtil) throws
                                                                                                          QueGGMissingFactoryClassException {
    SentenceBuilder sentenceBuilder = new SentenceBuilderCopulativePP(
      getLanguage(),
      getFrameType(),
      getSentenceTemplateRepository(),
      getSentenceTemplateParser()
    );

    return sentenceBuilder.generateFullSentences(getBindingVariable(), lexicalEntryUtil);
  }

  @Override
  public GrammarEntry generateFragmentEntry(
    GrammarEntry grammarEntry,
    LexicalEntryUtil lexicalEntryUtil
  ) throws QueGGMissingFactoryClassException {
    GrammarEntry fragmentEntry = copyGrammarEntry(grammarEntry);
    fragmentEntry.setType(SentenceType.NP);

    SentenceBuilder sentenceBuilder = new SentenceBuilderCopulativePP(
      getLanguage(),
      getFrameType(),
      getSentenceTemplateRepository(),
      getSentenceTemplateParser()
    );
    List<String> generatedSentences = sentenceBuilder.generateNP(getBindingVariable(),
                                                                 new String[]{"prepositionalAdjunct"},
                                                                 lexicalEntryUtil
    );
    generatedSentences = generatedSentences.stream().distinct().collect(Collectors.toList());
    fragmentEntry.setSentences(generatedSentences);

    return fragmentEntry;
  }
}
