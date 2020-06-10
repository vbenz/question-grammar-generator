package grammar.sparql.querycomponent;

public enum SelectVariable {
  SUBJECT_OF_PROPERTY("subjOfProp"),
  OBJECT_OF_PROPERTY("objOfProp");

  private String name;

  SelectVariable(String name) {
    this.name = name;
  }

  public String getVariableName() {
    return name;
  }
}

