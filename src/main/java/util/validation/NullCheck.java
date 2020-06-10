package util.validation;

import util.exceptions.QGGMissingFieldDeclarationException;

import static java.util.Objects.isNull;

public class NullCheck {
  public static void notNull(String objectName, Object object, Class clazz) throws QGGMissingFieldDeclarationException {
    if (isNull(object)) {
      throw new QGGMissingFieldDeclarationException(String.format("%s must be set in %s", objectName, clazz.getName()));
    }
  }
}
