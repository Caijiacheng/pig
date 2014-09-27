package com.mm.tinylove.error;

public class UnmarshalException extends TinyLoveException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4673733165907096086L;

	
    /**
     * Creates a new instance.
     */
	public UnmarshalException() {
    }
	
    /**
     * Creates a new instance.
     */
    public UnmarshalException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public UnmarshalException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public UnmarshalException(Throwable cause) {
        super(cause);
    }
	
}
