package util.exceptions;

public class QGGRequestException extends Exception {
    public QGGRequestException(String message) {
        super(message);
    }
    public QGGRequestException(String message, Throwable err) {
        super(message, err);
    }
}
