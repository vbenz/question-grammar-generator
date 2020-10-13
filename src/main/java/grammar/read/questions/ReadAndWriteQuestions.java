package grammar.read.questions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author elahi
 */
public class ReadAndWriteQuestions {
    private LinkedHashMap<String,String> questionAnswers=new LinkedHashMap<String,String>();

    public void readQuestionAnswers(String outputDir) throws IOException, Exception {
        List<File> list = getFiles(outputDir, "grammar_FULL_DATASET_EN", ".json");
        if (list.isEmpty()) {
            throw new Exception("No property files to process!!");
        }
        for (File file : list) {
            ObjectMapper mapper = new ObjectMapper();
            GrammarEntries grammarEntries = mapper.readValue(file, GrammarEntries.class);
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {
                    String answer="Answer will be "+grammarEntryUnit.getReturnType()+" and can be found by running sparql for "+grammarEntryUnit.getReturnVariable()
                            +" "+grammarEntryUnit.getSparqlQuery();
                for (String question : grammarEntryUnit.getSentences()) {
                      this.replaceVariables(question,grammarEntryUnit.getBindingList(),answer);
                }
               
            }
        }
        
        for(String question:questionAnswers.keySet()){
            System.out.println(question);
            //System.out.println(questionAnswers.get(question));
        }
    }

    public static List<File> getFiles(String fileDir, String category, String extension) {
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
            String line = tupple.getEntry() + "=" + tupple.getUri();
            str += line + "\n";
        }
        return str;
    }

    private void replaceVariables(String question, List<UriLabel> uriLabels,String answer) {
        if (question.contains("(") && question.contains(")")) {
            String result = StringUtils.substringBetween(question, "(", ")");
            question = question.replace("(", "");
            question = question.replace(")", "");
            for (UriLabel uriLabel : uriLabels) {
                question = question.replace(result, uriLabel.getLabel());
                questionAnswers.put(question,answer);
            }
        }
    }

}
