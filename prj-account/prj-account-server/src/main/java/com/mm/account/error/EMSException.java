package com.mm.account.error;

@SuppressWarnings("serial")
public class EMSException extends AccountException {
	 public EMSException() {
	    }

	    /**
	     * Creates a new instance.
	     */
	    public EMSException(String message, Throwable cause) {
	        super(message, cause);
	    }

	    /**
	     * Creates a new instance.
	     */
	    public EMSException(String message) {
	        super(message);
	    }

	    /**
	     * Creates a new instance.
	     */
	    public EMSException(Throwable cause) {
	        super(cause);
	    }
}
