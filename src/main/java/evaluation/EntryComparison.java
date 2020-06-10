package evaluation;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class EntryComparison implements Serializable {
  public static final long serialVersioUID = 2L;

  private List<Entry> queGGEntries = new ArrayList<>();
  private Entry qaldEntry;
  private List<String> qaldResults;
  float tp = 0, fp = 0, fn = 0, precision = 0, recall = 0;

  public void addTp(float newValue) {
    tp += newValue;
  }

  public void addFp(float newValue) {
    fp += newValue;
  }

  public void addFn(float newValue) {
    fn += newValue;
  }

  public void addQueGGEntry(Entry entry) {
    queGGEntries.add(entry);
  }
}
