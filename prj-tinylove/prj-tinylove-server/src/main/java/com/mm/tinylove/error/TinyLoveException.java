package com.mm.tinylove.error;

/**
 * An {@link Exception} which is thrown by a codec.
 */
public class TinyLoveException extends RuntimeException {

    private static final long serialVersionUID = -1464830400709348473L;

    /**
     * Creates a new instance.
     */
    public TinyLoveException() {
    }

    /**
     * Creates a new instance.
     */
    public TinyLoveException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public TinyLoveException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public TinyLoveException(Throwable cause) {
        super(cause);
    }
}
