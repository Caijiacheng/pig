package com.mm.account.error;

/**
 * An {@link Exception} which is thrown by a codec.
 */
public class AccountException extends RuntimeException {

    private static final long serialVersionUID = -1464830400709348473L;

    /**
     * Creates a new instance.
     */
    public AccountException() {
    }

    /**
     * Creates a new instance.
     */
    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public AccountException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public AccountException(Throwable cause) {
        super(cause);
    }
}
