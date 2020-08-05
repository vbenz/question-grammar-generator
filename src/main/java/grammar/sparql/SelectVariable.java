package grammar.sparql;

public enum SelectVariable {
  SUBJECT_OF_PROPERTY("subjOfProp"),
  OBJECT_OF_PROPERTY("objOfProp"),
  IS_A("isA");

  private final String name;

  SelectVariable(String name) {
    this.name = name;
  }

  public String getVariableName() {
    return name;
  }

  public String mapDomainOrRange() {
    return equals(SUBJECT_OF_PROPERTY) ? "domain" : "range";
  }
}

