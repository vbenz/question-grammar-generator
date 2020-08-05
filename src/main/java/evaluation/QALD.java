package evaluation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QALD {
  @JsonProperty
  public QALDDatasetDefinition dataset;
  @JsonProperty
  public List<QALDQuestions> questions;

  public static class QALDDatasetDefinition {
    @JsonProperty
    String id;
  }

  public static class QALDQuestion {
    @JsonProperty
    public String language;
    @JsonProperty
    public String string;
    @JsonProperty
    public String keywords;
  }

  public static class QALDQuery {
    @JsonProperty
    public String sparql;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class QALDQuestions {
    @JsonProperty
    public String id;
    @JsonProperty
    public String answertype;
    @JsonProperty
    public List<QALDQuestion> question;
    @JsonProperty
    public QALDQuery query;
  }
}
