package grammar.read.questions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author elahi
 */
public class ReadAndWriteQuestions {

    private LinkedHashMap<String, String> questionAnswers = new LinkedHashMap<String, String>();
    private String inputFileName = null;
    private String quesAnsStr = null;

    public ReadAndWriteQuestions(String QUESTION_ANSWER_LOCATION, String QUESTION_ANSWER_FILE, String outputDir, String outputFile) {
        this.inputFileName = QUESTION_ANSWER_LOCATION + File.separator + QUESTION_ANSWER_FILE;
        this.quesAnsStr = "";

        try {
            List<File> list = getFiles(outputDir, outputFile, ".json");
            if (list.isEmpty()) {
                throw new Exception("No property files to process!!");
            }
            this.readQuestionAnswers(list);
            this.writeQuestionAnswers();
        } catch (Exception ex) {
            Logger.getLogger(ReadAndWriteQuestions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readQuestionAnswers(List<File> fileList) throws Exception {

        for (File file : fileList) {
            ObjectMapper mapper = new ObjectMapper();
            GrammarEntries grammarEntries = mapper.readValue(file, GrammarEntries.class);
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {
                String answer = "Answer will be " + grammarEntryUnit.getReturnType() + " and can be found by running sparql for " + grammarEntryUnit.getReturnVariable()
                        + " " + grammarEntryUnit.getSparqlQuery();
                for (String question : grammarEntryUnit.getSentences()) {
                    this.replaceVariables(question, grammarEntryUnit.getBindingList(), answer);
                }

            }
        }

    }

    private void writeQuestionAnswers()
            throws IOException {
        this.getContent();
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.inputFileName));
        writer.write(this.quesAnsStr);
        writer.close();

    }

    private void getContent() {
        for (String question : questionAnswers.keySet()) {
            String line = question + "=" + questionAnswers.get(question);
            this.quesAnsStr += line ;
        }

        /*for (String question : questionAnswers.keySet()) {
            String questionLine = "Q: " + question + "\n";
            String answerLine = "A: " + questionAnswers.get(question);
            str += questionLine + answerLine;
        }
        return str;*/
    }

    private List<File> getFiles(String fileDir, String category, String extension) {
        String[] files = new File(fileDir).list();
        List<File> selectedFiles = new ArrayList<File>();
        for (String fileName : files) {
            if (fileName.contains(category) && fileName.contains(extension)) {
                selectedFiles.add(new File(fileDir + fileName));
            }
        }

        return selectedFiles;

    }

    public static String output(List<Tupple> inputTupples) throws IOException {
        String str = "";
        for (Tupple tupple : inputTupples) {
            String line = tupple.getEntry() + " = " + tupple.getUri();
            str += line + "\n";
        }
        return str;
    }

    private void replaceVariables(String question, List<UriLabel> uriLabels, String answer) {
        if (question.contains("(") && question.contains(")")) {
            String result = StringUtils.substringBetween(question, "(", ")");
            question = question.replace("(", "");
            question = question.replace(")", "");
            for (UriLabel uriLabel : uriLabels) {
                question = question.replace(result, uriLabel.getLabel());
                questionAnswers.put(question, answer);
            }
        }
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public String getQuesAnsStr() {
        return quesAnsStr;
    }

}
