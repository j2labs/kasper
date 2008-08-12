package com.kasper.core.exceptions;

public class NonPluggableException extends KasperException {
	private static final long serialVersionUID = -3650366406573324934L;
	
	public final static String WRONG_TYPE = "WRONG_TYPE";

    public NonPluggableException() {
        super();
    }

    public NonPluggableException( String message ) {
        super( message );
    }

}
