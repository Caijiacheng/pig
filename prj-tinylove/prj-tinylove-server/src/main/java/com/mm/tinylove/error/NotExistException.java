package com.mm.tinylove.error;

/**
 * An {@link Exception} which is thrown by a codec.
 */
public class NotExistException extends TinyLoveException {

    private static final long serialVersionUID = -1464830400709348473L;

    /**
     * Creates a new instance.
     */
    public NotExistException() {
    }

    /**
     * Creates a new instance.
     */
    public NotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public NotExistException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public NotExistException(Throwable cause) {
        super(cause);
    }
}
