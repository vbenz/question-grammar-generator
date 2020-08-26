import com.fasterxml.jackson.databind.ObjectMapper;
import evaluation.QALD;
import evaluation.QALDImporter;
import grammar.structure.component.GrammarWrapper;
import grammar.structure.component.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static util.io.ResourceHelper.loadResource;

/**
 * The tests in this class are all disabled.
 * If you want to start the evaluation process, you need to comment out the @Disabled flags first.
 * The evaluation needs the two output files of QueGG.
 * They need to be copied into the test resources folder manually. By default there are only minimal files present.
 */
class EvaluateAgainstQALDTest {

  public static final String QALD_FILE_ORIGINAL = "QALD-2017/qald-7-train-multilingual.json";
  public static final String QALD_FILE_MODIFIED = "QALD-2017/qald-7-train-multilingual_modified.json";
  private final EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(Language.EN);
  private GrammarWrapper grammarWrapper;
  private static QALD QALD_ORIGINAL;
  private static QALD QALD_MODIFIED;

  @BeforeEach
  void init() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    URL grammarEntriesFile = loadResource("grammar_FULL_DATASET_EN.json", this.getClass());
    URL grammarEntriesFile2 = loadResource("grammar_COMBINATIONS_EN.json", this.getClass());
    grammarWrapper = objectMapper.readValue(grammarEntriesFile, GrammarWrapper.class);
    GrammarWrapper gw2 = objectMapper.readValue(grammarEntriesFile2, GrammarWrapper.class);
    grammarWrapper.merge(gw2);

    QALDImporter qaldImporter = new QALDImporter();
    QALD_ORIGINAL = qaldImporter.readQald(QALD_FILE_ORIGINAL);
    QALD_MODIFIED = qaldImporter.readQald(QALD_FILE_MODIFIED);
  }

  /**
   * This does not actually test anything it is only there to generate the evaluation files.
   * Generates an evaluation on the original file (with ASK queries)
   */
  @Disabled
  @Test
  void testEvaluateAndOutputWithOriginalWithASK() throws IOException {
    evaluateAgainstQALD.evaluateAndOutput(grammarWrapper, QALD_ORIGINAL, QALD_ORIGINAL, true);
  }

  /**
   * This does not actually test anything it is only there to generate the evaluation files.
   * Generates an evaluation on the original file (without ASK queries)
   */
  @Disabled
  @Test
  void testEvaluateAndOutputWithOriginalWithoutASK() throws IOException {
    evaluateAgainstQALD.evaluateAndOutput(grammarWrapper, QALD_ORIGINAL, QALD_ORIGINAL, false);
  }

  /**
   * This does not actually test anything it is only there to generate the evaluation files.
   * Generates an evaluation on the modified file (with ASK queries)
   */
  @Disabled
  @Test
  void testEvaluateAndOutputWithModifiedWithASK() throws IOException {
    evaluateAgainstQALD.evaluateAndOutput(grammarWrapper, QALD_ORIGINAL, QALD_MODIFIED, true);
  }

  /**
   * This does not actually test anything it is only there to generate the evaluation files.
   * Generates an evaluation on the modified file (without ASK queries)
   */
  @Disabled
  @Test
  void testEvaluateAndOutputWithModifiedWithoutASK() throws IOException {
    evaluateAgainstQALD.evaluateAndOutput(grammarWrapper, QALD_ORIGINAL, QALD_MODIFIED, false);
  }
}
