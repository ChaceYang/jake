package io.github.funcfoo.id;

public class JakeIdException extends RuntimeException {
    static final long serialVersionUID = -725766903495102185L;

    public JakeIdException() {
    }

    public JakeIdException(String message) {
        super(message);
    }

    public JakeIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public JakeIdException(Throwable cause) {
        super(cause);
    }

    public JakeIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
