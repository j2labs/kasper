package com.kasper.ms;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;

import com.kasper.ms.MessageConstants;

public class Message implements Serializable {
	private static final long serialVersionUID = -5693316400597188463L;
	
	private int _type = MessageConstants.TYPE_NONE.code();
    private int _who = MessageConstants.WHO_NONE.code();
    
    public Message() {
    }

    public Message( int type, int who ) {
        _type = type;
        _who = who;
    }

    public void setWho( int who ) { _who = who; }
    public int getWho() { return _who; }

    public int getType() { return _type; }
    public void setType( int type ) { _type = type; }

    public void additionalReadProcessing( DataInputStream dis ) throws IOException, ClassNotFoundException {}
    public void additionalWriteProcessing( DataOutputStream dos ) throws IOException {}

    public void readStream( DataInputStream dis )
        throws IOException, ClassNotFoundException
    {
        _type = dis.readInt();
        _who = dis.readInt();

        additionalReadProcessing( dis );
    }

    public void writeStream( DataOutputStream dos )
        throws IOException
    {
        dos.writeInt( _type );
        dos.writeInt( _who );

        additionalWriteProcessing( dos );
    }

    public String toString() {
        return "Type: " + _type
            + " - Who: " + _who;
    }
}
