package com.kasper.ms;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;

import com.kasper.ms.MessageConstants;
import com.kasper.core.Log;

public class MessageContainer implements Serializable {
	private static final long serialVersionUID = -639827232845857072L;
	
	private int _type = MessageConstants.TYPE_NONE.code();
    private Message _msg = null;
    
    public MessageContainer() {
    }

    public MessageContainer( Message msg ) {
        _type = msg.getType();
        _msg = msg;
    }

    public int getType() { return _type; }

    public Message getMessage() { return _msg; }

    public Message getMessageByType( int type ) {
        if( type < MessageConstants.TYPE_START_VAL ) {
            return new Message();
        }
        else if (type == MessageConstants.TYPE_CHAT.code() ) {
            return new ChatMessage();
        }

        return null;
    }

    public final void readStream( DataInputStream dis )
        throws IOException, ClassNotFoundException
    {
        _type = dis.readInt();

        _msg = getMessageByType( _type );
        _msg.readStream( dis );
    }

    public final void writeStream( DataOutputStream dos )
        throws IOException
    {
        Log.debug( "Message type: " + _type );
        dos.writeInt( _type );
        _msg.writeStream( dos );
    }

    public String toString() {
        return "MessageContainer type: " + _type
            + _msg.toString();
    }
}
