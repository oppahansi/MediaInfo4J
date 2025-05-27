package de.oppa.mi4j;

public class MediaInfoException extends RuntimeException {
    public MediaInfoException(String message) {
        super(message);
    }

    public MediaInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}
