package lexicon;

import eu.monnetproject.lemon.LemonModel;
import eu.monnetproject.lemon.model.LemonElement;
import eu.monnetproject.lemon.model.LexicalEntry;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.Lexicon;
import grammar.generator.OWLRestriction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.stream.Collectors;

public class LexiconPrinter {
  private static Logger LOG = LogManager.getLogger(LexiconPrinter.class);

  /**
   * Usage: <br>
   *   <pre>
   * {@code
   *   PrintWriter pw = new PrintWriter(System.out);
   *   LexiconPrinter.printLexicaFromModel(model, pw);
   *   pw.flush();
   *   pw.close();
   * }</pre>
   * @param model a lemon model to print
   * @param writer any writer
   */
  public static void printLexicaFromModel(LemonModel model, Writer writer) {
    model.getLexica().forEach(lexi -> {
      lexi.getEntrys().forEach(lexicalEntry -> {
        String entryString = entryToString(lexi, lexicalEntry);
        try {
          writer.append(entryString);
        } catch (IOException e) {
          LOG.error("Could not write lexicon entry.");
        }
      });
      try {
        writer.append("Number of entries: ").append(String.valueOf(lexi.getEntrys().size())).append("\n");
      } catch (IOException e) {
        LOG.error("Could not write lexicon entries.");
      }
    });
  }

  private static String entryToString(Lexicon lexicom, LexicalEntry lexicalEntry) {
    try {
      String onProperty = "";
      String hasValue = "";
      if (!lexicalEntry.getSenses().isEmpty() && lexicalEntry.getSenses().iterator().hasNext()) {
        OWLRestriction owlRestriction = new OWLRestriction(lexicom, lexicalEntry, lexicalEntry.getSenses().iterator().next()).invoke();
        onProperty = owlRestriction.getProperty();
        hasValue = owlRestriction.getValue();
      }
      return "-".repeat(40) + "\n" +

          String.format("%40s  %s\n", "getURI()", lexicalEntry.getURI().toString()) +
          String.format("%40s  %s\n", "getCanonicalForm().getWrittenRep()", lexicalEntry.getCanonicalForm().getWrittenRep()) +
          String.format("%40s  %s\n", "getForms()", lexicalEntry.getForms().toString()) +
          String.format("%40s      %s\n", "getForms() ... getWrittenRep()", lexicalEntry.getForms().stream().map(LexicalForm::getWrittenRep).collect(Collectors.toList()).toString()) +
          String.format("%40s      %s\n", "getForms() ... getFormVariants()", lexicalEntry.getForms().stream().map(LexicalForm::getFormVariants).collect(Collectors.toList()).toString()) +
          String.format("%40s      %s\n", "getForms() ... getAnnotations()", lexicalEntry.getForms().stream().map(LemonElement::getAnnotations).collect(Collectors.toList()).toString()) +
          String.format("%40s      %s\n", "getForms() ... getPropertys()", lexicalEntry.getForms().stream().map(LemonElement::getPropertys).collect(Collectors.toList()).toString()) +
          String.format("%40s  %s\n", "getSynBehaviors()", lexicalEntry.getSynBehaviors().toString()) +
          String.format("%40s      %s\n", "getSynBehaviors() ... getSynArgs()", getPropertyForAll(lexicalEntry.getSynBehaviors().iterator(), "getSynArgs")) +
          String.format("%40s  %s\n", "getSenses()", lexicalEntry.getSenses().toString()) +
          String.format("%40s      %s\n", "getSenses() ... getReference()", getPropertyForAll(lexicalEntry.getSenses().iterator(), "getReference")) +
          String.format("%40s      %s\n", "getSenses() ... getContexts()", getPropertyForAll(lexicalEntry.getSenses().iterator(), "getContexts")) +
          String.format("%40s      %s\n", "getSenses() ... getSubjOfProps()", getPropertyForAll(lexicalEntry.getSenses().iterator(), "getSubjOfProps")) +
          String.format("%40s      %s\n", "getSenses() ... getObjOfProps()", getPropertyForAll(lexicalEntry.getSenses().iterator(), "getObjOfProps")) +
          String.format("%40s      %s\n", "getAnnotations() ... onProperty", onProperty) +
          String.format("%40s      %s\n", "getAnnotations() ... hasValue", hasValue) +
          String.format("%40s  %s\n", "getPropertys()", lexicalEntry.getPropertys().toString()) +
          String.format("%40s  %s\n", "getAnnotations()", lexicalEntry.getAnnotations().toString()) +
          String.format("%40s  %s\n", "getLexicalVariants()", lexicalEntry.getLexicalVariants().toString()) +

          "-".repeat(40) + "\n";
    } catch (NullPointerException  npe) {
      LOG.error("Could not print entry {}", lexicalEntry.getURI().toString());
    }
    return "";
  }

  private static String getPropertyForAll(Iterator<?> iterator, String methodName) {
    StringBuilder sb = new StringBuilder();
    while(iterator.hasNext()) {
      Object obj = iterator.next();
      Class<?> clazz = obj.getClass();
      try {
        Method method = clazz.getMethod(methodName);
        sb.append(method.invoke(obj));
        sb.append(" ");
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        LOG.error("Failed to execute method {} in class {}", methodName, clazz);
      }
    }
    return sb.toString();
  }
  /*

  private static Restriction getRestrictionFromPropertyRef(String file, String propertyRef) {
//              if (entry.getSenses() != null) {
//                if (entry.getSenses().iterator().hasNext()) {
//                  LexicalSense lexicalSense = entry.getSenses().iterator().next();
//                  if (lexicalSense.getReference() != null) {
//                    if (lexicalSense.getReference().toString().startsWith(BASE_URL)) {
//                      Restriction restriction = getRestrictionFromPropertyRef(file.toString(), entry.getSenses().iterator().next().getReference().toString());
//                      System.out.println(restriction.toString());
//                    }
//                  }
//                }
//              }
  Model model = ModelFactory.createDefaultModel() ;
    model.read(file) ;
  Resource r = ((Resource)((Resource) model.listStatements(null, LEMON.canonicalForm, (RDFNode) null).next().getSubject()
      .listProperties(LEMON.sense).next().getObject())
      .getProperty(LEMON.reference).getObject());
    return new Restriction(
      propertyRef,
      r.getProperty(OWL.hasValue).getObject().toString(),
        r.getProperty(OWL.onProperty).getObject().toString()
    );
}
   */
}
