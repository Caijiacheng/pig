package com.mm.account.error;

public class DBException extends AccountException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4673733165907096086L;

	
    /**
     * Creates a new instance.
     */
	public DBException() {
    }
	
    /**
     * Creates a new instance.
     */
    public DBException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public DBException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public DBException(Throwable cause) {
        super(cause);
    }
	
}
