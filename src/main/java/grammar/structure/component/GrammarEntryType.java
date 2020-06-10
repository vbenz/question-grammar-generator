package grammar.structure.component;

import java.util.stream.Stream;

public enum GrammarEntryType {
  SENTENCE,
  NP;

  public static GrammarEntryType getMatchingType(String name) {
    return Stream.of(GrammarEntryType.values())
        .filter(enumValue -> enumValue.name().equals(name))
        .findFirst()
        .orElse(null);
  }
}
