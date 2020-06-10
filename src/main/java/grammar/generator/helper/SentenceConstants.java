package grammar.generator.helper;


import grammar.structure.component.Language;

import java.util.HashMap;
import java.util.Map;

public class SentenceConstants {
  private Map<Language, String> languageisStringMap;
  private Map<Language, Map<SubjectType, String>> languageQWordMap;
  private final String isEn = "is";
  private final String isDe = "ist";
  private final String whoEn = "Who";
  private final String whoDe = "Wer";
  private final String whatEn = "What";
  private final String whatDe = "Was";
  private final String whomEn = "whom";
  private final String whomDe = "wem";

  public SentenceConstants() {
    this.languageisStringMap = createLanguageIsStringMap();
    this.languageQWordMap = createLanguageQWordMap();
  }

  public String getIsStringForLanguage(Language language) {
    return languageisStringMap.get(language);
  }

  private Map<Language, Map<SubjectType, String>> createLanguageQWordMap() {
    Map<Language, Map<SubjectType, String>> languageQWordMap = new HashMap<>();
    Map<SubjectType, String> subjectTypeStringMapDe = new HashMap<>();
    Map<SubjectType, String> subjectTypeStringMapEn = new HashMap<>();
    subjectTypeStringMapDe.put(SubjectType.PERSON, whoDe);
    subjectTypeStringMapDe.put(SubjectType.THING, whatDe);
    subjectTypeStringMapDe.put(SubjectType.PERSON2, whomDe);
    subjectTypeStringMapEn.put(SubjectType.PERSON, whoEn);
    subjectTypeStringMapEn.put(SubjectType.THING, whatEn);
    subjectTypeStringMapEn.put(SubjectType.PERSON2, whomEn);
    languageQWordMap.put(Language.DE, subjectTypeStringMapDe);
    languageQWordMap.put(Language.EN, subjectTypeStringMapEn);
    return languageQWordMap;
  }

  private Map<Language,String> createLanguageIsStringMap() {
    Map<Language, String> languageisStringMap = new HashMap<>();
    languageisStringMap.put(Language.DE, isDe);
    languageisStringMap.put(Language.EN, isEn);
    return languageisStringMap;
  }

  public Map<Language, Map<SubjectType, String>> getLanguageQWordMap() {
    return languageQWordMap;
  }
}
