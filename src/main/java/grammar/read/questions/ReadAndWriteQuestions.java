package grammar.read.questions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import grammar.read.questions.CreateTree;
import grammar.read.questions.GrammarEntries;
import grammar.read.questions.GrammarEntryUnit;
import grammar.read.questions.UriLabel;
import grammar.read.questions.Tupple;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elahi
 */
public class ReadAndWriteQuestions {

   
   /* public static void main(String[] args) throws IOException, Exception {
        ReadAndWriteQuestions guiMain=new ReadAndWriteQuestions();
        guiMain.readQuestionAnswers();
        String content = "";
        CreateTree createTree = new CreateTree(QUESTION_ANSWER_LOCATION, QUESTION_ANSWER_FILE);
        content = output(createTree.getInputTupples());
        //System.out.println(content);
        //read question data
        //read();
    }*/

    public void readQuestionAnswers(String outputDir) throws IOException, Exception {
        List<File> list = getFiles(outputDir, "grammar_FULL_DATASET_EN", ".json");
        if (list.isEmpty()) {
            throw new Exception("No property files to process!!");
        }
        for (File file : list) {

            ObjectMapper mapper = new ObjectMapper();
            GrammarEntries grammarEntries = mapper.readValue(file, GrammarEntries.class);
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {

                List<String> questions = grammarEntryUnit.getSentences();
                for (String question : grammarEntryUnit.getSentences()) {
                    System.out.println(question);
                }
                List<UriLabel> uriLabels = grammarEntryUnit.getBindingList();
                for (UriLabel uriLabel : uriLabels) {
                    System.out.println(uriLabel.getLabel());
                    System.out.println(uriLabel.getUri());
                }
            }
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

  

}
