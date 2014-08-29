package com.mm.account.error;

public class UnknowAccException extends AccountException {

	private static final long serialVersionUID = 1L;
	
    /**
     * Creates a new instance.
     */
    public UnknowAccException() {
    }
	
    /**
     * Creates a new instance.
     */
    public UnknowAccException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public UnknowAccException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public UnknowAccException(Throwable cause) {
        super(cause);
    }
}
