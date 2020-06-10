package grammar.structure.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Binding {
  private String label;
  private String uri;

  public String toString() {
    return "\n        Binding(label = " + this.getLabel() + ", uri = " + this.getUri() + ")";
  }
}
