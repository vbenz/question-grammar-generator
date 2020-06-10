import evaluation.Entry;
import evaluation.EntryComparison;
import evaluation.EvaluationResult;
import grammar.generator.helper.BindingConstants;
import grammar.sparql.SPARQLRequest;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.GrammarWrapper;
import grammar.structure.component.Language;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.matcher.PatternMatchHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static grammar.generator.helper.BindingConstants.DEFAULT_BINDING_VARIABLE;
import static java.util.Objects.isNull;

public class EvaluateAgainstQALD {
  private Logger log = LogManager.getLogger(EvaluateAgainstQALD.class);
  private Model model;
  private final boolean printNoMatch = false;

  EvaluateAgainstQALD() {
    model = ModelFactory.createDefaultModel();
    evaluateAndOutput();
  }


  public static void main(String[] args) {
    new EvaluateAgainstQALD();
  }

  private void evaluateAndOutput() {
    String resultFileName = "QALD-QueGG-Comparison_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm")) + ".csv";
    QALDImporter qaldImporter = new QALDImporter();
    QALDImporter.QALD qald = qaldImporter.readQuald(QALDImporter.QALD_FILE);
    try {
      QueGG queGG = new QueGG();
      GrammarWrapper grammarWrapper = queGG.combineGrammars(Language.EN);
      ZonedDateTime before = ZonedDateTime.now();
      EvaluationResult result = doEvaluation(qald, grammarWrapper);
      ZonedDateTime after = ZonedDateTime.now();
      long duration = Duration.between(before, after).toSeconds();

//      qaldImporter.writeToCSV(result, resultFileName);
      log.info(String.format("Evaluation was completed in %dmin %ds", duration / 60, duration % 60));
//      System.out.println("Results are available here: " + resultFileName);
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
  }

  private EvaluationResult doEvaluation(QALDImporter.QALD qaldFile, GrammarWrapper grammarWrapper) {
    List<String[]> list = new ArrayList<>();
    list.add(new String[]{"QALD id", "QALD answertype", "QALD question", "QALD sparql", "QueGG question", "QueGG sparql", "QALD <-> QueGG question", "QALD <-> QueGG sparql"});
    int listSizeOffset = list.size();

    EvaluationResult evaluationResult = new EvaluationResult();

    for (QALDImporter.QALD.QALDQuestions qaldQuestions : qaldFile.questions) {
      EntryComparison entryComparison = new EntryComparison();
      for (GrammarEntry grammarEntry : grammarWrapper.getGrammarEntries()) {
        compareEntries(qaldQuestions, grammarEntry, entryComparison);
      }
      evaluationResult.getEntryComparisons().add(entryComparison);
      log.info("tp: {}, fp: {}, fn: {}", entryComparison.getTp(), entryComparison.getFp(), entryComparison.getFn());
      evaluationResult.setTp_global(evaluationResult.getTp_global() + entryComparison.getTp());
      evaluationResult.setFp_global(evaluationResult.getFp_global() + entryComparison.getFp());
      evaluationResult.setFn_global(evaluationResult.getFn_global() + entryComparison.getFn());
      log.info("tp_global: {}, fp_global: {}, fn_global: {}", evaluationResult.getTp_global(), evaluationResult.getFp_global(), evaluationResult.getFn_global());
    }
//          matches.forEach(grammarEntry -> {
//                list.add(
//                    new String[]{
//                        qaldQuestions.id,
//                        qaldQuestions.answertype,
//                        QALDImporter.getQualdQuestionString(qaldQuestions, "en"),
//                        qaldQuestions.query.sparql,
//                        grammarEntry.getSentences().stream().reduce((x, y) -> x + "\n" + y).orElse(""),
//                        grammarEntry.getSparqlQuery(),
//                        String.valueOf(isQuestionMatch(qaldQuestions, grammarEntry)),
//                        String.valueOf(isSparqlMatch(qaldQuestions, grammarEntry))
//                    }
//                );
//              }
//          );

    evaluationResult.setPrecision_global(evaluationResult.getTp_global() / (evaluationResult.getTp_global() + evaluationResult.getFp_global()));
    evaluationResult.setRecall_global(evaluationResult.getTp_global() / (evaluationResult.getTp_global() + evaluationResult.getFn_global()));

    log.info("-".repeat(50) );
    log.info("tp_global: {}, fp_global: {}, fn_global: {}", evaluationResult.getTp_global(), evaluationResult.getFp_global(), evaluationResult.getFn_global());
    log.info("precision_global: {}, recall_global: {}", evaluationResult.getPrecision_global(), evaluationResult.getRecall_global());


    FileOutputStream f;
    try {
      f = new FileOutputStream(new File("evaluationResultObject.txt"));
      ObjectOutputStream o = new ObjectOutputStream(f);

      // Write objects to file
      o.writeObject(evaluationResult);
      o.close();
      f.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    // Add stats
    System.out.println(list.size());
    System.out.println(qaldFile.questions.size());
    System.out.println((float) (list.size() - listSizeOffset) / (float) qaldFile.questions.size());
    list.add(new String[]{String.format("Total matches: %d", list.size() - listSizeOffset),
        String.format("QALD coverage: %.2f%%", (float) (list.size() - listSizeOffset) / (float) qaldFile.questions.size() * 100),
        "", "", "", "", "", ""});
    return evaluationResult;
  }

  /**
   * QueGG Satz-Prototyp: Who wrote $x?
   * - $x ersetzen durch regex mit capture
   * - regex auf Qald anwenden
   * -> capture group in bindings labels von QueGG suchen
   * -> uri nehmen und in QueGG sparql einsetzen, Ergebnisse der sparql queries (QueGG <-> QALD) vergleichen
   */
  private void compareEntries(QALDImporter.QALD.QALDQuestions qaldQuestions, GrammarEntry grammarEntry, EntryComparison entryComparison) {
    String qaldQuestion = QALDImporter.getQualdQuestionString(qaldQuestions, "en");
    String cleanQaldQuestion = cleanQALDString(qaldQuestion);
    String qaldSparql = qaldQuestions.query.sparql;
    String queGGSparql = grammarEntry.getSparqlQuery();

    Entry qaldEntry = new Entry();
    qaldEntry.setId(qaldQuestions.id);
    qaldEntry.setQuestions(qaldQuestion);
    qaldEntry.setSparql(qaldSparql);
    if (isNull(entryComparison.getQaldEntry())) {
      entryComparison.setQaldEntry(qaldEntry);
    }

    Entry queGGEntry = new Entry();
    queGGEntry.setId(grammarEntry.getId());
    queGGEntry.setQuestions(grammarEntry.getSentences().stream().reduce((x, y) -> x + "\n" + y).orElse(""));
    queGGEntry.setSparql(queGGSparql);
    entryComparison.addQueGGEntry(queGGEntry);

    // Extend model if there are any new prefixes in the QALD query
    updateModelWithNewPrefixes(qaldSparql);
    if (printNoMatch) {
      log.info("-".repeat(100));
    }
    log.debug("Processing grammar entry {} and QALD sentence: {}", grammarEntry.getSentences(), qaldQuestion);

    // Replace grammarEntry sentence DEFAULT_BINDING_VARIABLE with a regex capture.
    List<Pattern> cleanGrammarSentences = grammarEntry.getSentences().stream().map(this::cleanString).map(Pattern::compile).collect(Collectors.toList());
    String uriMatch = "";
    String varName = "";
    List<String> uriResultListQueGG = new ArrayList<>();
    List<String> uriResultListQALD;
    String matchedPattern = "";
    for (Pattern sentencePattern : cleanGrammarSentences) {

      // Try to find an item to replace $x in the QueGG sentence
      String matchedSentenceItem = PatternMatchHelper.getPatternMatch(cleanQaldQuestion, sentencePattern);
      if (matchedSentenceItem.isEmpty()) {
        // Could not match QueGG sentence pattern on qaldSentence
        continue;
      }

      // Try to find the matching uri for the previously found item inside the QALD SPARQL query
      String propertyMatchPattern = String.format("[%s%s]{%d,}[_\\(\\)\\w]*", matchedSentenceItem.toLowerCase().replace(" ", "_"), matchedSentenceItem.toUpperCase().replace(" ", "_"), matchedSentenceItem.length());
      Pattern sparqlPattern = Pattern.compile(String.format("(?<prefix>\\w+)*:*(?<match>%s)", propertyMatchPattern));
      String prefixedProperty = PatternMatchHelper.getPatternMatch(qaldSparql, sparqlPattern, 0);
      if (prefixedProperty.isEmpty()) {
        // Could not find a matching prefixed property inside the QALD SPARQL query
        continue;
      }
      String prefixMatch = PatternMatchHelper.getPatternMatch(qaldSparql, sparqlPattern, "prefix");
      if (prefixMatch.isEmpty()) {
        // we already have a uri, so we search for that:
        sparqlPattern = Pattern.compile(String.format("(<[:\\w\\.\\/]+%s>)", propertyMatchPattern));
        uriMatch = PatternMatchHelper.getPatternMatch(qaldSparql, sparqlPattern);
      } else {
        // we need to parse a prefix for this uri
        uriMatch = String.format("<%s>", model.expandPrefix(prefixedProperty));
        if (!uriMatch.startsWith("<http") || uriMatch.contains(" ")) {
          uriMatch = "";
          log.debug("Could not expand prefix {} in QALD SPARQL query\n{}", prefixMatch, qaldSparql);
        }
      }

      // get variable name (i.e. objOfProp, subjOfProp) from QueGG binding sparql to replace it in the main sparql query
      varName = getVarNameFromQueGGBindingSparqlQuery(grammarEntry);
      if (!varName.isEmpty() && !uriMatch.isEmpty()) {
        matchedPattern = sentencePattern.toString();
        break;
      }
    }
    log.debug("Executing QALD SPARQL Query:\n{}", qaldSparql);
    // Only execute qaldQuery once
    if (isNull(entryComparison.getQaldResults())) {
      SPARQLRequest sparqlRequestQALD = stringToSparql(qaldSparql);
      uriResultListQALD = getSparqlResultList(sparqlRequestQALD);
      entryComparison.setQaldResults(uriResultListQALD);
    } else {
      uriResultListQALD = entryComparison.getQaldResults();
    }

    if (varName.isEmpty() || uriMatch.isEmpty()) {
      // Variable name to substitute in the bindings SPARQL or a valid uri substitute could not be found
      if (printNoMatch) {
        log.info("No Match!");
      }
    } else {
      // Execute QueGG and QALD SPARQL queries and compare results
      // replace binding variable with the previously found QALD uri match and execute the query
//      log.info(
//          "Comparing QueGG query to QALD query\n{}\n{}",
//          sepString("QueGG", queGGSparql.replace(varName, uriMatch)),
//          sepString("QALD", qaldSparql)
//      );
      log.info("Match found with pattern '{}'", matchedPattern);
      queGGEntry.setSparql(queGGSparql.replace(varName, uriMatch));

      log.info("Creating QueGG SPARQL Query:\n{}", queGGSparql.replace(varName, uriMatch));
      SPARQLRequest sparqlRequestQueGG = stringToSparql(queGGSparql.replace(varName, uriMatch));
      uriResultListQueGG = getSparqlResultList(sparqlRequestQueGG);
      log.info("QueGG Query results: {}", uriResultListQueGG);
      log.info("QALD Query results: {}", uriResultListQALD);
    }

    log.debug("Comparing QueGG results to QALD results: #QueGG: {}, #QALD: {}", uriResultListQueGG.size(), uriResultListQALD.size());
    log.debug("Comparing QueGG results to QALD results: QueGG: {}, QALD: {}", uriResultListQueGG, uriResultListQALD);
      /*
        true positive:  Anzahl Ergebnisse meiner SPARQL Query, die auch in QALD Query Ergebnis sind
        false positive: Anzahl Ergebnisse meiner SPARQL Query, die nicht in QALD Query Ergebnis sind
        false negative: Anzahl Ergebnisse der QALD query, die nicht in meinem SPARQL Query Ergebnis sind
        true negative: in keinem Datensatz enthalten, nicht relevant.

        Precision: tp / (tp + fp)
        Recall: tp / (tp + fn)

        Lokal: Measure Pro Satz / Eintrag
        Global: Measure für Summe aller Sätze (tp_global, fp_global, fn_global)
      */
    List<String> finalUriResultListQueGG = uriResultListQueGG;

    if (entryComparison.getTp() == 0) {
      entryComparison.setTp(uriResultListQueGG.containsAll(uriResultListQALD) ?
          uriResultListQueGG.size() :
          uriResultListQueGG.stream().filter(uriResultListQALD::contains).count());
    }
      entryComparison.addFp(uriResultListQueGG.stream().filter(resultQueGG -> !uriResultListQALD.contains(resultQueGG)).count());
    // only add fn if tp is not already set
    if (entryComparison.getFn() == 0 && entryComparison.getTp() == 0) {
      entryComparison.setFn(uriResultListQALD.stream().filter(resultQald -> !finalUriResultListQueGG.contains(resultQald)).count());
    } else if (entryComparison.getTp() != 0) {
      entryComparison.setFn(0);
    }
    if (entryComparison.getTp() > 0) {
      entryComparison.setPrecision(entryComparison.getTp() / (entryComparison.getTp() + entryComparison.getFp()));
      entryComparison.setRecall(entryComparison.getTp() / (entryComparison.getTp() + entryComparison.getFn()));
    }
    log.debug("tp: {}, fp: {}, fn: {}", entryComparison.getTp(), entryComparison.getFp(), entryComparison.getFn());
    log.debug("Precision: {}, Recall: {}", entryComparison.getPrecision(), entryComparison.getRecall());
  }

  private String getVarNameFromQueGGBindingSparqlQuery(GrammarEntry grammarEntry) {
    return grammarEntry.getSparqlParameterMapping().get(grammarEntry.getBindings().getBindingVariableName());
  }

  private void updateModelWithNewPrefixes(String qaldSparql) {
    Pattern prefixPattern = Pattern.compile("PREFIX\\s*(\\w+):\\s*<([\\w/.:]+)>");
    Matcher qaldSparqlMatcher = prefixPattern.matcher(qaldSparql);
    while (qaldSparqlMatcher.find()) {
      if (!model.getNsPrefixMap().containsKey(qaldSparqlMatcher.group(1))) {
        model.setNsPrefix(qaldSparqlMatcher.group(1), qaldSparqlMatcher.group(2));
      }
    }
  }

  private SPARQLRequest stringToSparql(String sparqlString) {
    SPARQLRequest sparqlRequest = new SPARQLRequest();
    ParameterizedSparqlString ps = new ParameterizedSparqlString();
    ps.setCommandText(sparqlString);
    sparqlRequest.setParameterizedSparqlString(ps);
    return sparqlRequest;
  }

  private List<String> getSparqlResultList(SPARQLRequest sparqlRequest) {
    List<String> resultList = new ArrayList<>();
    String match = PatternMatchHelper.getPatternMatch(sparqlRequest.getParameterizedSparqlString().getCommandText(), Pattern.compile("(ASK\\s+)"), 0);
    if (!match.isEmpty()) {
      resultList.add(String.valueOf(sparqlRequest.executeAskQuery()));
      return resultList;
    }
    ResultSet results = sparqlRequest.executeSelectQuery();
    while (results.hasNext()) {
      QuerySolution qs = results.next();

      try {
        Iterator<Var> varIterator = (((ResultBinding) qs).getBinding()).vars();
        while (varIterator.hasNext()) {
          Var var = varIterator.next();
          if (var.getVarName().equals(BindingConstants.LABEL)) {
            continue;
          }
          if (qs.get(var.getVarName()).isURIResource()) {
            resultList.add(qs.get(var.getVarName()).asResource().getURI());
          } else if (qs.get(var.getVarName()).isLiteral()) {
            resultList.add(qs.get(var.getVarName()).asLiteral().toString());
          } else if (qs.get(var.getVarName()).isAnon()) {
            resultList.add(qs.get(var.getVarName()).asNode().toString());
          } else if (qs.get(var.getVarName()).isResource()) {
            resultList.add(qs.get(var.getVarName()).asResource().toString());
          }
        }
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    return resultList;
  }

  /**
   * Replace all "the", make lower case, add regex capture for $x
   */
  private String cleanString(String sentence) {
    return sentence.replaceAll("the *", "")
        .replace(DEFAULT_BINDING_VARIABLE, "([\\w\\s\\d-.]+)")
        .replace("?", "\\?")
        .toLowerCase()
        .trim();
  }

  private String cleanQALDString(String sentence) {
    return sentence.replaceAll("the ", "")
        .toLowerCase()
        .trim();
  }
}
