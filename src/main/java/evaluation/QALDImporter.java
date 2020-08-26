package evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static util.io.ResourceHelper.loadResource;

public class QALDImporter {
  private static final Logger LOG = LogManager.getLogger(QALDImporter.class);

  public QALDImporter() {}

  public void qaldToCSV(String qaldFile, String outputFile) throws IOException {
    QALD qald = readQald(qaldFile);
    writeToCSV(qaldJsonToCSVTemplate(qald), outputFile);
  }

  public QALD readQald(String file) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    URL qaldFile = loadResource(file, this.getClass());
    return objectMapper.readValue(qaldFile, QALD.class);
  }

  public void writeToCSV(List<String[]> dataLines, String fileName) throws IOException {
    CSVWriter writer = new CSVWriter(new FileWriter(fileName), '\t', '"', '"', "\n");
    dataLines.forEach(writer::writeNext);
    writer.close();
  }

  private List<String[]> qaldJsonToCSVTemplate(QALD qaldFile) {
    List<String[]> list = new ArrayList<>();
    list.add(new String[]{"id", "answertype", "question", "sparql"});
    qaldFile.questions.forEach(
      qaldQuestions ->
        list.add(
          new String[]{
            qaldQuestions.id,
            qaldQuestions.answertype,
            getQaldQuestionString(qaldQuestions, "en"),
            qaldQuestions.query.sparql
          }
        )
    );
    return list;
  }

  public static String getQaldQuestionString(QALD.QALDQuestions qaldQuestions, String languageAbbreviation) {
    return qaldQuestions.question.stream()
                                 .filter(qaldQuestion -> qaldQuestion.language.startsWith(languageAbbreviation))
                                 .findFirst()
                                 .orElseThrow().string;
  }
}
