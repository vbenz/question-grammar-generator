package grammar.sparql.querycomponent;

public enum Prefix {
  RDFS("http://www.w3.org/2000/01/rdf-schema#"),
  DBO("http://dbpedia.org/ontology/"),
  DBR("http://dbpedia.org/resource/"),
  LEXINFO("http://www.lexinfo.net/ontology/2.0/lexinfo#"),
  LEMON("http://lemon-model.net/lemon#");

  private String uri;

  Prefix(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }
}
