/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import grammar.read.result.GrammarEntries;
import grammar.read.result.GrammarEntryUnit;
import grammar.read.result.UriLabel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elahi
 */
public class GuiMain {

    private static String outputDir = "src/main/resources/lexicon/en/nouns/new/output/";

    public static void main(String[] args) throws IOException, Exception {
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

            /*ObjectMapper mapper = new ObjectMapper();
            GrammarEntryUnit grammarEntryUnit = mapper.readValue(file, GrammarEntryUnit.class);
            List<UriLabel> uriLabels=grammarEntryUnit.getSentenceBindings().getBindingList();
            for (UriLabel uriLabel:uriLabels){
                 //System.out.println(uriLabel.getLabel());
                 //System.out.println(uriLabel.getUri());
            }
            List<String> questions=new ArrayList<String>();
            for (String question:grammarEntryUnit.getSentences()){
                System.out.println(question);
            }*/
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

}
