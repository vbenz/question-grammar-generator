package grammar.generator.helper;

import java.util.stream.Stream;

public enum SubjectType {
  PERSON, // who
  PERSON2, // whom
  THING;

  public static SubjectType getMatchingType(String name) {
    return Stream.of(SubjectType.values())
        .filter(subjectType -> subjectType.name().equals(name))
        .findFirst()
        .orElse(THING);
  }
}
