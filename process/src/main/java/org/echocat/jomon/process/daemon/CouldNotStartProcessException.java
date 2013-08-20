package org.echocat.jomon.process.daemon;

public class CouldNotStartProcessException extends RuntimeException {

    public CouldNotStartProcessException() {
    }

    public CouldNotStartProcessException(String message) {
        super(message);
    }

    public CouldNotStartProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public CouldNotStartProcessException(Throwable cause) {
        super(cause);
    }
}
