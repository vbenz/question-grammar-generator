import com.fasterxml.jackson.databind.ObjectMapper;
import grammar.structure.component.GrammarWrapper;
import grammar.structure.component.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static util.io.ResourceHelper.loadResource;

class EvaluateAgainstQALDTest {

  private final EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(Language.EN);
  private GrammarWrapper grammarWrapper;

  @BeforeEach
  void init() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    URL grammarEntriesFile = loadResource("grammar_FULL_DATASET_EN.json", this.getClass());
    URL grammarEntriesFile2 = loadResource("grammar_COMBINATIONS_EN.json", this.getClass());
    grammarWrapper = objectMapper.readValue(grammarEntriesFile, GrammarWrapper.class);
    GrammarWrapper gw2 = objectMapper.readValue(grammarEntriesFile2, GrammarWrapper.class);
    grammarWrapper.merge(gw2);
  }

  /**
   * This does not actually test anything it is only there to generate the evaluation files.
   */
  @Disabled
  @Test
  void testEvaluateAndOutput() throws IOException {
    evaluateAgainstQALD.evaluateAndOutput(grammarWrapper);
  }
}
