package util.exceptions;

public class QGGMissingFieldDeclarationException extends Exception {
    public QGGMissingFieldDeclarationException(String message) {
        super(message);
    }
    public QGGMissingFieldDeclarationException(String message, Throwable err) {
        super(message, err);
    }
}
