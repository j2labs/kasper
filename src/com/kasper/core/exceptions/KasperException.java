package com.kasper.core.exceptions;

import java.lang.Exception;
import java.io.Serializable;

public class KasperException extends Exception implements Serializable {
	private static final long serialVersionUID = 2572289687077361131L;

	public KasperException() {
        super();
    }

    public KasperException( String message ) {
        super( message );
    }
}
