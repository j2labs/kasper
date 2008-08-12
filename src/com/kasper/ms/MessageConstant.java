package com.kasper.ms;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;

// Used by any class that collects message constants
final public class MessageConstant implements Serializable {
	private static final long serialVersionUID = 1571038651026323701L;
	
	private int _code;
    private String _name;
    private String _desc;

    MessageConstant( int code, String name, String desc ) {
        _code = code;
        _name = name;
        _desc = desc;
    }

    final public int code() {
        return _code;
    }

    final public String name() {
        return _name;
    }

    final public String desc() {
        return _desc;
    }

    final public boolean equals( Object obj ) {
        if ( obj instanceof MessageConstant ) {
            return ( _code == ( (MessageConstant) obj ).code() );
        }
        else {
            return false;
        }
    }

    public void readStream( DataInputStream dis )
        throws IOException, ClassNotFoundException
    {
        _code = dis.readInt();
    }

    public void writeStream( DataOutputStream dos )
        throws IOException
    {
        dos.writeInt( _code );
    }

}
