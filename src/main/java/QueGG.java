
import eu.monnetproject.lemon.LemonModel;
import grammar.generator.BindingResolver;
import grammar.generator.GrammarRuleGeneratorRoot;
import grammar.generator.GrammarRuleGeneratorRootImpl;
import grammar.read.questions.CreateTree;
import grammar.read.questions.ReadAndWriteQuestions;
import static grammar.read.questions.ReadAndWriteQuestions.output;
import grammar.structure.component.DomainOrRangeType;
import grammar.structure.component.FrameType;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.GrammarWrapper;
import grammar.structure.component.Language;
import lexicon.LexiconImporter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import java.util.logging.Level;

@NoArgsConstructor
public class QueGG {
    //temporarly closed. becuase it does not work from command line
    //private static final Logger LOG = LogManager.getLogger(QueGG.class);

    private static String inputDir = "src/main/resources/lexicon/en/nouns/input/";
    private static String outputDir = "src/main/resources/lexicon/en/nouns/new/output/";
    public static String QUESTION_ANSWER_LOCATION = "/home/elahi/recent/question-grammar-generator/src/main/resources";
    public static String QUESTION_ANSWER_FILE = "questions.txt";

    public static void main(String[] args) {
        QueGG queGG = new QueGG();
        Integer menu = 2;
        String content = "";

        if (menu == 1) {
            generateQuestions(args, queGG);
        } else if (menu == 2) {
            ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions();
            try {
                //readAndWriteQuestions.readQuestionAnswers(outputDir);
                CreateTree createTree = new CreateTree(QUESTION_ANSWER_LOCATION, QUESTION_ANSWER_FILE);
                content = output(createTree.getInputTupples());
                System.out.println(content);

            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private static void generateQuestions(String[] args, QueGG queGG) {
        Language language = Language.stringToLanguage("EN");

        try {
            if (args.length < 3) {
                System.out.println("running on default parameter!!");
                /*System.out.println("language:"+language);
                System.out.println("inputDir:"+inputDir);
                System.out.println("outputDir:"+outputDir);  */
            } else {
                language = Language.stringToLanguage(args[0]);
                inputDir = Path.of(args[1]).toString();
                outputDir = Path.of(args[2]).toString();
            }

            //LOG.info("Starting {} with language parameter '{}'", QueGG.class.getName(), language);
            //LOG.info("Input directory: {}", inputDir);
            //LOG.info("Output directory: {}", outputDir);
            queGG.init(language, inputDir, outputDir);
            //LOG.warn("To get optimal combinations of sentences please add the following types to {}\n{}",
            //        DomainOrRangeType.class.getName(), DomainOrRangeType.MISSING_TYPES.toString()
           // );
        } catch (IllegalArgumentException | IOException e) {
            System.err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.printf("Usage: <%s> <input directory> <output directory>%n", Arrays.toString(Language.values()));
        }
    }

    private void init(Language language, String inputDir, String outputDir) throws IOException {
        try {
            loadInputAndGenerate(language, inputDir, outputDir);
        } catch (URISyntaxException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            //LOG.error("Could not create grammar: {}", e.getMessage());
        }
    }

    private void loadInputAndGenerate(Language lang, String inputDir, String outputDir) throws
            IOException,
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException,
            URISyntaxException {
        LexiconImporter lexiconImporter = new LexiconImporter();
        LemonModel lemonModel = lexiconImporter.loadModelFromDir(inputDir, lang.toString().toLowerCase());
        generateByFrameType(lang, lemonModel, outputDir);
    }

    private void generateByFrameType(Language language, LemonModel lemonModel, String outputDir) throws
            IOException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        GrammarWrapper grammarWrapper = new GrammarWrapper();
        for (FrameType frameType : FrameType.values()) {
            if (!isNull(frameType.getImplementingClass())) {
                GrammarWrapper gw = generateGrammarGeneric(
                        lemonModel,
                        (GrammarRuleGeneratorRoot) frameType.getImplementingClass()
                                .getDeclaredConstructor(Language.class)
                                .newInstance(language)
                );
                grammarWrapper.merge(gw);
            }
        }
        // Make a GrammarRuleGeneratorRoot instance to use the combination function
        GrammarRuleGeneratorRoot generatorRoot = new GrammarRuleGeneratorRootImpl(language);
        //LOG.info("Start generation of combined entries");
        grammarWrapper.getGrammarEntries().addAll(generatorRoot.generateCombinations(grammarWrapper.getGrammarEntries()));

        for (GrammarEntry grammarEntry : grammarWrapper.getGrammarEntries()) {
            grammarEntry.setId(String.valueOf(grammarWrapper.getGrammarEntries().indexOf(grammarEntry) + 1));
        }

        // Output file is too big, make two files
        GrammarWrapper regularEntries = new GrammarWrapper();
        regularEntries.setGrammarEntries(
                grammarWrapper.getGrammarEntries()
                        .stream()
                        .filter(grammarEntry -> !grammarEntry.isCombination())
                        .collect(Collectors.toList())
        );
        GrammarWrapper combinedEntries = new GrammarWrapper();
        combinedEntries.setGrammarEntries(
                grammarWrapper.getGrammarEntries().stream().filter(GrammarEntry::isCombination).collect(Collectors.toList())
        );

        // Generate bindings
        //LOG.info("Start generation of bindings");
        grammarWrapper.getGrammarEntries().forEach(generatorRoot::generateBindings);

        generatorRoot.dumpToJSON(
                Path.of(outputDir,
                        "grammar_" + generatorRoot.getFrameType().getName() + "_" + language + ".json").toString(),
                regularEntries
        );
        generatorRoot.dumpToJSON(Path.of(outputDir, "grammar_COMBINATIONS" + "_" + language + ".json").toString(), combinedEntries);

        // Insert those bindings and write new files
        //LOG.info("Start resolving bindings");
        BindingResolver bindingResolver = new BindingResolver(grammarWrapper.getGrammarEntries());
        grammarWrapper = bindingResolver.resolve();
        generatorRoot.dumpToJSON(Path.of(outputDir, "grammar_FULL_WITH_BINDINGS_" + language + ".json").toString(), grammarWrapper);

    }

    private GrammarWrapper generateGrammarGeneric(LemonModel lemonModel, GrammarRuleGeneratorRoot grammarRuleGenerator) {
        GrammarWrapper grammarWrapper = new GrammarWrapper();
        lemonModel.getLexica().forEach(lexicon -> {
            //LOG.info("Start generation for FrameType {}", grammarRuleGenerator.getFrameType().getName());
            grammarRuleGenerator.setLexicon(lexicon);
            grammarWrapper.setGrammarEntries(grammarRuleGenerator.generate(lexicon));
        });
        return grammarWrapper;
    }

}
