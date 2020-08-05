package grammar.sparql;

public enum Prefix {
  RDFS("http://www.w3.org/2000/01/rdf-schema#"),
  DBPEDIA("http://dbpedia.org/"),
  DBO(DBPEDIA.getUri() + "ontology/"),
  DBR(DBPEDIA.getUri() + "resource/"),
  LEXINFO("http://www.lexinfo.net/ontology/2.0/lexinfo#"),
  LEMON("http://lemon-model.net/lemon#");

  private final String uri;

  Prefix(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }
}
