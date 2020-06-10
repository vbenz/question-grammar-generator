package grammar.sparql;

import grammar.generator.helper.BindingConstants;
import grammar.sparql.querycomponent.SelectVariable;
import grammar.structure.component.Language;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.RDFS;
import util.exceptions.QGGMissingFieldDeclarationException;

import static util.validation.NullCheck.notNull;

public class SPARQLRequest {

  public static final String SPARQL_ENDPOINT_URL = "http://dbpedia.org/sparql";

  private ParameterizedSparqlString parameterizedSparqlString;
  private SelectVariable selectVariable;
  private String searchProperty;
  private final String labelString = BindingConstants.LABEL;
  private ElementTriplesBlock block;
  private ElementGroup body;
  private Query query;

  /**
   * Constructs a full SPARQL query. <br>
   * Label search and language filtering can be added later.
   *
   */
  public SPARQLRequest() {
    this.body = new ElementGroup();
    this.block = new ElementTriplesBlock();
    this.parameterizedSparqlString = new ParameterizedSparqlString();
    this.query = initQuery();
  }

  public void addTriple(Triple triple) {
    block.addTriple(triple);
  }

  /**
   * Add ?subjOfProp < searchProperty_uri > ?objOfProp
   */
  public void addGenericTriple() throws QGGMissingFieldDeclarationException {
    notNull("searchProperty", searchProperty, this.getClass());
    Triple triple =
      Triple.create(
        Var.alloc(SelectVariable.SUBJECT_OF_PROPERTY.getVariableName()),
        NodeFactory.createURI(searchProperty),
        Var.alloc(SelectVariable.OBJECT_OF_PROPERTY.getVariableName()));
    block.addTriple(triple);
  }

  public void createQuery() throws QGGMissingFieldDeclarationException {
    notNull("searchProperty", searchProperty, this.getClass());
    notNull("selectVariable", selectVariable, this.getClass());
    this.parameterizedSparqlString = initParameterizedSparqlString();
  }

  private ParameterizedSparqlString initParameterizedSparqlString() {
    // do not forget to add at least generic triple!

/*
    body.addElement(block); // Group pattern match and filter

    query.setDistinct(true);
    query.setQueryPattern(body);                               // Set the body of the query to our group
    query.addResultVar(selectVariable.getVariableName());      // Select ___
*/
    parameterizedSparqlString.setCommandText(block.getPattern().toString());
    return parameterizedSparqlString;
  }

  private Query initQuery() {
    Query query = QueryFactory.make();
//    query.setQuerySelectType();                                // Make it a select query
    return query;
  }

  /**
   * This adds a label query and a language filter to the query. <br>
   * {@code
   * select ... ?label
   * ...
   * ?<selectVariable> rdfs:label ?label .
   * FILTER langMatches( lang(?label),"<filterLanguage>" ) .
   * } <br>
   * <selectVariable> will be replaced by the selectVariable value. <br>
   * <filterLanguage> will be replaced by the filterLanguage value. <br>
   */
  public void addLabelQueryWithFilter(Language filterLanguage) throws QGGMissingFieldDeclarationException {
    notNull("selectVariable", selectVariable, this.getClass());
    // ?subjOfProp rdfs:label ?label .
    Triple labelPattern =
      Triple.create(
        Var.alloc(selectVariable.getVariableName()),
        RDFS.label.asNode(),
        Var.alloc(labelString));
    block.addTriple(labelPattern);

    // FILTER langMatches( lang(?label), \"DE\" )
    Expr labelExpr = new ExprVar(labelString);            // ?label
    Expr langExpr = new E_Lang(labelExpr);                // lang(?label)
    Expr LangMatchesExpr = new E_LangMatches(
      langExpr,
      new NodeValueString(filterLanguage.toString())      // langMatches(lang(?label), \"DE\")
    );
    ElementFilter filter = new ElementFilter(LangMatchesExpr); // FILTER _____
    body.addElement(filter);
    query.addResultVar(Var.alloc(labelString));
  }

  /**
   * Adds a limit to the query.
   * <b>Needs to be called last before executing the query.</b>
   *
   * @param limit a limit > 0.
   */
  public void addLimit(int limit) {
    if (limit > 0) {
      this.query.setLimit(limit);
    }
  }

  /**
   * Needs a valid <b>SPARQLEndpointUrl</b> and a valid <b>parameterizedSparqlString</b>.
   *
   * @return a ResultSet after execution.
   */
  public ResultSet executeSelectQuery() {
    QueryExecution exec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URL, parameterizedSparqlString.asQuery());
    exec.setTimeout(30000);
    return exec.execSelect();
  }

  /**
   * Needs a valid <b>SPARQLEndpointUrl</b> and a valid <b>parameterizedSparqlString</b>.
   *
   * @return true or false as answer to the yes/now question
   */
  public boolean executeAskQuery() {
    QueryExecution exec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT_URL, parameterizedSparqlString.asQuery());
    exec.setTimeout(30000);
    return exec.execAsk();
  }

  public String toString() {
    return parameterizedSparqlString.toString();
  }

  public ParameterizedSparqlString getParameterizedSparqlString() {
    return parameterizedSparqlString;
  }

  public void setPrefix(String prefix, String uri) {
    parameterizedSparqlString.setNsPrefix(prefix, uri);
  }

  public void setParameterizedSparqlString(ParameterizedSparqlString parameterizedSparqlString) {
    this.parameterizedSparqlString = parameterizedSparqlString;
  }

  public void setSelectVariable(SelectVariable selectVariable) {
    this.selectVariable = selectVariable;
  }

  public SelectVariable getSelectVariable() {
    return selectVariable;
  }

  public void setSearchProperty(String searchProperty) {
    this.searchProperty = searchProperty;
  }

  public String getSearchProperty() {
    return searchProperty;
  }

  public ElementTriplesBlock getBlock() {
    return block;
  }
}
