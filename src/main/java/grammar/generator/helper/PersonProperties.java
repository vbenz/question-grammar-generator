package grammar.generator.helper;

import static grammar.sparql.querycomponent.Prefix.DBO;

public enum PersonProperties {
  DBO_PERSON(DBO.getUri() + "Person"),
  WIKI_PERSON("http://www.wikidata.org/entity/Q215627"),
  DBO_AGENT(DBO.getUri() + "Agent");

  private String uri;

  PersonProperties(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }
}
