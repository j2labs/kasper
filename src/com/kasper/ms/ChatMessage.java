package com.kasper.ms;

import com.kasper.ms.Message;
import com.kasper.ms.ChatMessageConstants;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ChatMessage extends Message {
	private static final long serialVersionUID = 9052309086068543844L;
	
	int _chatMsgType;
    String _nick;
    String _chatText;
    String _channel;

    public ChatMessage() {
        super();
    }

    public ChatMessage(int who, int chatMsgType, String nick, String chatText, String channel) {
        super( ChatMessageConstants.TYPE_CHAT.code(), who );

        _chatMsgType = chatMsgType;
        _nick = nick;
        _chatText = chatText;
        _channel = channel;
    }

    public int getType() { return ChatMessageConstants.TYPE_CHAT.code(); }

    public int getChatMsgType() { return _chatMsgType; }
    public void setChatMsgType( int chatMsgType ) { _chatMsgType = chatMsgType; }

    public String getNick() { return _nick; }
    public void setNick( String nick ) { _nick = nick; }

    public String getChatText() { return _chatText; }
    public void setChatText( String chatText ) { _chatText = chatText; }

    public String getChannel() { return _channel; }
    public void setChannel( String channel ) { _channel = channel; }

    public String toString() {
        return super.toString() + "\nNick: " + _nick
            + "\nChat msg type: " + _chatMsgType
            + "\nChat text: " + _chatText
            + "\nChannel: " + _channel + "\n";
    }

    public void additionalReadProcessing( DataInputStream dis)
        throws IOException, ClassNotFoundException
    {
        _chatMsgType = dis.readInt();
        _nick = dis.readUTF();
        _chatText = dis.readUTF();
        _channel = dis.readUTF();
    }
    
    public void additionalWriteProcessing( DataOutputStream dos )
        throws IOException
    {
        dos.writeInt( _chatMsgType );
        dos.writeUTF( _nick );
        dos.writeUTF( _chatText );
        dos.writeUTF( _channel );
    }
}
