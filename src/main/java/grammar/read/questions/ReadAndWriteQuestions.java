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
    public static String FRAMETYPE_NPP = "NPP";

    public ReadAndWriteQuestions() {

    }

    public ReadAndWriteQuestions(String QUESTION_ANSWER_LOCATION, String QUESTION_ANSWER_FILE, String outputDir, String outputFile) throws Exception {
        this.inputFileName = QUESTION_ANSWER_LOCATION + File.separator + QUESTION_ANSWER_FILE;
        List<File> list = getFiles(outputDir, outputFile, ".json");
        if (list.isEmpty()) {
            throw new Exception("No property files to process!!");
        }
        this.readQuestionAnswers(list);
        for(String key:questionAnswers.keySet()){
            System.out.println(key);
             System.out.println(questionAnswers.get(key));

        }
        //this.writeQuestionAnswers();
    }

    private void readQuestionAnswers(List<File> fileList) throws Exception {
        String sparql = null;
        for (File file : fileList) {
            ObjectMapper mapper = new ObjectMapper();
            GrammarEntries grammarEntries = mapper.readValue(file, GrammarEntries.class);
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {
                sparql = grammarEntryUnit.getSparqlQuery();
                for (String question : grammarEntryUnit.getSentences()) {
                    this.replaceVariables(question, grammarEntryUnit.getBindingList(), sparql, grammarEntryUnit.getFrameType());
                }

            }
        }
    }

    private void replaceVariables(String question, List<UriLabel> uriLabels, String sparql, String frameType) {
        String result =null;
        if (question.contains("(") && question.contains(")")) {
            result = StringUtils.substringBetween(question, "(", ")");
            question = question.replace(result, "X");
        }
        
            for (UriLabel uriLabel : uriLabels) {
                String questionT=question.replaceAll("(X)", uriLabel.getLabel());
                questionT= questionT.replace("(", "");
                questionT= questionT.replace(")", "");
                String answer = this.getAnswer(uriLabel.getUri(), sparql, frameType);
                System.out.println( questionT+" "+answer);
                questionAnswers.put(questionT, answer);
            }
        
    }

    public String getAnswer(String subjProp, String sparql, String syntacticFrame) {
        String property = null;
        if (syntacticFrame.contains(FRAMETYPE_NPP)) {
            property = StringUtils.substringBetween(sparql, "<", ">");
        }
        return new SparqlQuery(subjProp, property).getObject();
    }

    private String getContent() {
        String quesAnsStr = "";
        for (String question : questionAnswers.keySet()) {
            String line = question + "=" + questionAnswers.get(question);
            quesAnsStr += line;
        }
        return quesAnsStr;
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

    private void writeQuestionAnswers()
            throws IOException {
        String quesAnsStr = this.getContent();
        System.out.println(quesAnsStr);
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.inputFileName));
        writer.write(quesAnsStr);
        writer.close();

    }

 
}
