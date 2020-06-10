package evaluation;

import lombok.Data;

import java.io.Serializable;

@Data
public class Entry implements Serializable {
  public static final long serialVersioUID = 3L;
  private String id, questions, sparql;
}
