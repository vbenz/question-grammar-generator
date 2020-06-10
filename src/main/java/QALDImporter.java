import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static util.io.ResourceHelper.loadResource;

public class QALDImporter {

  public static final String QALD_FILE = "QALD-2017/qald-7-train-multilingual_modified.json";

  public QALDImporter() {
    qaldToCSV();
  }

  public static void main(String[] args) {
    new QALDImporter();
  }

  static class QALD {
    static class QALDDatasetDefinition {
      @JsonProperty
      String id;
    }

    static class QALDQuestion {
      @JsonProperty
      String language;
      @JsonProperty
      String string;
      @JsonProperty
      String keywords;
    }

    static class QALDQuery {
      @JsonProperty
      String sparql;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class QALDQuestions {
      @JsonProperty
      String id;
      @JsonProperty
      String answertype;
      @JsonProperty
      List<QALDQuestion> question;
      @JsonProperty
      QALDQuery query;
    }

    @JsonProperty
    QALDDatasetDefinition dataset;
    @JsonProperty
    List<QALDQuestions> questions;
  }

  static String getQualdQuestionString(QALD.QALDQuestions qaldQuestions, String languageAbbreviation) {
    return qaldQuestions.question.stream().filter(qaldQuestion -> qaldQuestion.language.startsWith(languageAbbreviation)).findFirst().orElseGet(QALD.QALDQuestion::new).string;
  }

  QALD readQuald(String file) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      URL lexiconFile = loadResource(file, this.getClass());
      return objectMapper.readValue(lexiconFile, QALD.class);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  void writeToCSV(List<String[]> dataLines, String fileName) throws IOException {
    CSVWriter writer = new CSVWriter(new FileWriter(fileName), '\t', '"', '"', "\n");
      dataLines
          .forEach(writer::writeNext);
    writer.close();
  }

  private void qaldToCSV() {
    try {
      QALD qald = readQuald(QALD_FILE);
      writeToCSV(qaldJsonToCSVTemplate(qald), "QALD-2017-dataset-raw.csv");
    } catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  private List<String[]> qaldJsonToCSVTemplate(QALD qaldFile) {
    List<String[]> list = new ArrayList<>();
    list.add(new String[]{"id", "answertype", "question", "sparql"});
    qaldFile.questions
        .forEach(qaldQuestions -> {
          list.add(new String[]{qaldQuestions.id, qaldQuestions.answertype, getQualdQuestionString(qaldQuestions, "en"), qaldQuestions.query.sparql});
          list.add(new String[]{qaldQuestions.id, "", getQualdQuestionString(qaldQuestions, "de"), ""});
        });
    return list;
  }
}
