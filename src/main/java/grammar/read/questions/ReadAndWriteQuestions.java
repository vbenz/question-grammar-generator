package grammar.read.questions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author elahi
 */
public class ReadAndWriteQuestions {

    private LinkedHashMap<String, String> questionAnswers = new LinkedHashMap<String, String>();
    private String content = null;
    public static String FRAMETYPE_NPP = "NPP";
    public ReadAndWriteQuestions(String questionAnswerFile)  {
        this.content = FileUtils.fileToString(questionAnswerFile);

    }

    public ReadAndWriteQuestions(String questionAnswerFile, String inputFileDir, String inputFile) throws Exception{
        List<File> list = FileUtils.getFiles(inputFileDir, inputFile, ".json");
        if (list.isEmpty()) {
            throw new Exception("No files to process for question answering system!!");
        } else {
            this.readQuestionAnswers(list);
        }
        this.content =this.prepareQuestionAnswerStr();

        FileUtils.stringToFile(this.content, questionAnswerFile);
    }

    private void readQuestionAnswers(List<File> fileList) throws Exception {
        String sparql = null;
        Integer index = 0;
        for (File file : fileList) {
            index = index + 1;
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
        String result = null;
        if (question.contains("(") && question.contains(")")) {
            result = StringUtils.substringBetween(question, "(", ")");
            question = question.replace(result, "X");
        } else if (question.contains("$x")) {
            System.out.println(question);

        }
        Integer index = 0;
        for (UriLabel uriLabel : uriLabels) {
            Boolean flag = false;
            index = index + 1;
            String questionT = question.replaceAll("(X)", uriLabel.getLabel());
            questionT = questionT.replace("(", "");
            questionT = questionT.replace(")", "");
            /*if (questionT.contains("$x")) {
                    flag=true;
                }*/
            questionT = questionT.replace("$x", uriLabel.getLabel());
            String answer = this.getAnswerFromWikipedia(uriLabel.getUri(), sparql, frameType);
            //if(!flag){
            System.out.println("questionT:" + questionT);
            System.out.println("answer:" + answer);
            //}

            questionAnswers.put(questionT, answer);

        }

    }

    public String getAnswerFromWikipedia(String subjProp, String sparql, String syntacticFrame) {
        String property = null;
        if (syntacticFrame.contains(FRAMETYPE_NPP)) {
            property = StringUtils.substringBetween(sparql, "<", ">");
        }
        return new SparqlQuery(subjProp, property).getObject();
    }

    private String prepareQuestionAnswerStr() {
        String quesAnsStr = "";
        for (String question : questionAnswers.keySet()) {
            String line = question + "=" + questionAnswers.get(question) + "\n";
            quesAnsStr += line;
        }
        return quesAnsStr;
    }


    public String getContent() {
        return content;
    }

}
