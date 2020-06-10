package grammar.structure.component;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static grammar.sparql.querycomponent.Prefix.DBO;

public enum DomainOrRangeType {
  PERSON(
      List.of(
          URI.create(DBO.getUri() + "Person"),
          URI.create("http://www.wikidata.org/entity/Q215627"),
          URI.create(DBO.getUri() + "Agent")
      )
  ),
  COUNTRY(List.of(URI.create("http://dbpedia.org/ontology/Country"))),
  THING(List.of(URI.create("http://www.w3.org/2002/07/owl#Thing"))),
  CURRENCY(List.of(URI.create("http://dbpedia.org/ontology/Currency"))),
  MAYOR(List.of(URI.create("http://dbpedia.org/ontology/Mayor"))),
  CITY(List.of(URI.create("http://dbpedia.org/ontology/City"))),
  BAND(List.of(URI.create("http://dbpedia.org/ontology/Band"))),
  LOCATION(List.of(URI.create("http://dbpedia.org/ontology/Location"))),
  TIMEZONE(List.of(URI.create("http://dbpedia.org/class/yago/TimeZone108691276")));

  private final List<URI> references;


  DomainOrRangeType(List<URI> refs) {
    this.references = refs;
  }

  public List<URI> getReferences() {
    return references;
  }

  public static DomainOrRangeType getMatchingType(URI uri) {
    return Stream.of(DomainOrRangeType.values())
        .filter(domainType -> domainType.references.stream().anyMatch(uri1 -> uri1.equals(uri)))
        .findFirst()
        .orElse(THING);
  }
}
