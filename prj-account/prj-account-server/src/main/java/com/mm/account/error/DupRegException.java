package com.mm.account.error;

@SuppressWarnings("serial")
public class DupRegException extends AccountException{
	
    /**
     * Creates a new instance.
     */
	public DupRegException() {
    }
	
    /**
     * Creates a new instance.
     */
    public DupRegException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public DupRegException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public DupRegException(Throwable cause) {
        super(cause);
    }
	
}
