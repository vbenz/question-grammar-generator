import eu.monnetproject.lemon.LemonModel;
import grammar.generator.AdjectiveAttributiveGrammarRuleGenerator;
import grammar.generator.GrammarRuleGeneratorRoot;
import grammar.generator.NPPGrammarRuleGenerator;
import grammar.generator.TransitiveVPGrammarRuleGenerator;
import grammar.structure.component.GrammarWrapper;
import grammar.structure.component.Language;
import lexicon.LexiconImporter;
import lexicon.LexiconPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

public class QueGG {
  private static Logger LOG = LogManager.getLogger(QueGG.class);

  QueGG() throws IOException {
    init();
  }

  public static void main(String[] args) throws IOException {
    new QueGG();
  }

  private void init() throws IOException {
    try {
//      getAdjectiveAttributiveGrammar(Language.EN);
//      getTransitiveGrammar(Language.EN);
      getNppGrammar(Language.EN);
//    combineGrammars(Language.EN);
    } catch (URISyntaxException e) {
      LOG.error("Could not create grammar");
    }
  }

  private GrammarWrapper generateGrammarGeneric(GrammarRuleGeneratorRoot grammarRuleGenerator, Language language) throws IOException, URISyntaxException {
    LexiconImporter lexiconImporter = new LexiconImporter();
    GrammarWrapper grammarWrapper = new GrammarWrapper();
    LemonModel model = lexiconImporter.loadModelFromDir(language.toString().toLowerCase());
    model.getLexica().forEach(lexicon -> {
      grammarRuleGenerator.setLexicon(lexicon);
      grammarWrapper.setGrammarEntries(grammarRuleGenerator.generate(lexicon));
    });
    grammarRuleGenerator.dumpToJSON("grammar_" + grammarRuleGenerator.getFrameType().getName() + "_" + language + ".json", grammarWrapper);

      PrintWriter pw = new PrintWriter(System.out);
      LexiconPrinter.printLexicaFromModel(model, pw);
      pw.flush();
      pw.close();
    return grammarWrapper;
  }

  private GrammarWrapper getTransitiveGrammar(Language language) throws IOException, URISyntaxException {
    TransitiveVPGrammarRuleGenerator grammarRuleGenerator = new TransitiveVPGrammarRuleGenerator(language);
    return generateGrammarGeneric(grammarRuleGenerator, language);
  }

  private GrammarWrapper getNppGrammar(Language language) throws IOException, URISyntaxException {
    NPPGrammarRuleGenerator grammarRuleGenerator = new NPPGrammarRuleGenerator(language);
    return generateGrammarGeneric(grammarRuleGenerator, language);
  }

  private GrammarWrapper getAdjectiveAttributiveGrammar(Language language) throws IOException, URISyntaxException {
    AdjectiveAttributiveGrammarRuleGenerator grammarRuleGenerator = new AdjectiveAttributiveGrammarRuleGenerator(language);
    return generateGrammarGeneric(grammarRuleGenerator, language);
  }

  GrammarWrapper combineGrammars(Language lang) throws IOException, URISyntaxException {
    GrammarWrapper tv = getTransitiveGrammar(lang);
    GrammarWrapper aa = getAdjectiveAttributiveGrammar(lang);
    GrammarWrapper npp = getNppGrammar(lang);

    tv.merge(aa);
    tv.merge(npp);

    for (int i = 0; i < tv.getGrammarEntries().size(); i++) {
      tv.getGrammarEntries().get(i).setId(String.valueOf(i + 1));
//      System.out.println(tv.getGrammarEntries().get(i).toString());
    }

    return tv;
  }
}
