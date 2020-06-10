package grammar.generator.helper.sentencetemplates;

import eu.monnetproject.lemon.LemonModel;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.Lexicon;
import grammar.generator.NPPGrammarRuleGenerator;
import grammar.structure.component.Language;
import lexicon.LexiconImporter;
import net.lexinfo.LexInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QueGGNPPSentenceImplTest {

  private LemonModel lemonModel;
  private NPPGrammarRuleGenerator nppGrammarRuleGenerator;

  @BeforeAll
  void setUp() throws IOException, URISyntaxException {
    LexiconImporter lexiconImporter = new LexiconImporter();
    lemonModel = lexiconImporter.loadModelFromDir("en");
    nppGrammarRuleGenerator = new NPPGrammarRuleGenerator(Language.EN);
  }
  @Test
  void testGenerateSentence() {
    for (Lexicon lexicon : lemonModel.getLexica()) {
      List<LexicalEntry> filteredEntries = nppGrammarRuleGenerator.getEntriesFilteredByGrammarType(lexicon);
      for (LexicalEntry lexicalEntry : filteredEntries) {
        System.out.println(lexicalEntry.getProperty(new LexInfo().getProperty("partOfSpeech")));
      }
    }
  }
}
