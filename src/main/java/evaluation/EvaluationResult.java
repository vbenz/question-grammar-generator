package evaluation;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class EvaluationResult implements Serializable {
  public static final long serialVersioUID = 1L;
  float tp_global = 0, fp_global = 0, fn_global = 0;
  float precision_micro = 0, recall_micro = 0, f_measure_micro = 0;
  float precision_macro = 0, recall_macro = 0, f_measure_macro = 0;
  List<EntryComparison> entryComparisons = new ArrayList<>();
}
