package grammar.structure.component;

import lombok.Data;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

@Data
public class VariableBindings {
  private String bindingVariableName;
  private List<Binding> bindingList;


  public String toString() {
    return "  VariableBindings(\n" +
      "    bindingVariableName =\t" + this.getBindingVariableName() + ",\n" +
      "    bindingList =\n      " + (!isNull(this.getBindingList()) ? this.getBindingList().toString() : Collections.EMPTY_LIST) + "\n  )";
  }
}
