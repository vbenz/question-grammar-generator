
import grammar.read.questions.ReadAndWriteQuestions;
import grammar.structure.component.Language;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * This class is responsible for creating question answering interface.
 */
/**
 *
 * @author elahi
 */
public class QAnterface {

    private static final Logger LOG = LogManager.getLogger(QueGG.class);
    private static String inputDir = "src/main/resources/lexicon/en/nouns/input/";
    private static String outputDir = "src/main/resources/lexicon/en/nouns/new/output/";
    public static String QUESTION_ANSWER_LOCATION = "src/main/resources";
    public static String QUESTION_ANSWER_FILE = "questions.txt";

    public static void main(String[] args) {
        QueGG queGG = new QueGG();

        Language language = Language.stringToLanguage("EN");

        if (args.length < 3) {
            System.out.println("running on default parameter!!");
        } else {
            language = Language.stringToLanguage(args[0]);
            inputDir = Path.of(args[1]).toString();
            outputDir = Path.of(args[2]).toString();
        }

        LOG.info("Starting {} with language parameter '{}'", QueGG.class.getName(), language);
        LOG.info("Input directory: {}", inputDir);
        LOG.info("Output directory: {}", outputDir);
    }

    private static void questionAnsweringInterface(String[] args, QueGG queGG) {
        String questionAnswerFile = QUESTION_ANSWER_LOCATION + File.separator + QUESTION_ANSWER_FILE;

        ReadAndWriteQuestions readAndWriteQuestions = null;
        Integer task = 3;
        String content = "";

        if (task.equals(2)) {
            try {
                readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile, outputDir, "grammar_FULL_DATASET_EN");
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (task.equals(3)) {
            readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile);
        }
    }

}
