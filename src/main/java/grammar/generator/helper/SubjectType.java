package grammar.generator.helper;

import grammar.structure.component.DomainOrRangeType;

import java.util.stream.Stream;

/**
 * A classification of question words.<br>
 * The ending "PRONOUN" implicitly suggests the usage of a single question word as subject,
 * whereas "DETERMINER" needs a noun.
 */
public enum SubjectType {
  PERSON_INTERROGATIVE_PRONOUN, // who
  INTERROGATIVE_DETERMINER,     // which
  THING_INTERROGATIVE_PRONOUN;  // what

  /**
   * Get the matching pronoun type (person or thing). The default value is "thing" if there is no clear match.<br>
   * The match is made by comparison to {@link DomainOrRangeType}.
   *
   * @param name is usually the {@link DomainOrRangeType} name of the condition
   * @return the matching {@code SubjectType} or {@link SubjectType#THING_INTERROGATIVE_PRONOUN} if nothing else matches.
   * @see DomainOrRangeType
   */
  public static SubjectType getMatchingType(String name) {
    return Stream.of(SubjectType.values())
                 .filter(subjectType -> subjectType.name().contains(name))
                 .findFirst()
                 .orElse(THING_INTERROGATIVE_PRONOUN);
  }

  /**
   * Check the specific SubjectType for the ending "PRONOUN".
   *
   * @return true if enum ends with "PRONOUN".
   */
  public boolean isPronoun() {
    return checkEnding("PRONOUN");
  }

  /**
   * Check the specific SubjectType for the ending "DETERMINER".
   *
   * @return true if enum ends with "DETERMINER".
   */
  public boolean isDeterminer() {
    return checkEnding("DETERMINER");
  }

  private boolean checkEnding(String ending) {
    return this.toString().endsWith(ending);
  }
}
