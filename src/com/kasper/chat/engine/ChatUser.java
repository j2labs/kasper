package com.kasper.chat.engine;

import java.net.Socket;
import java.lang.String;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import com.kasper.core.Log;

public class ChatUser {

    private Socket _socket = null;
    private String _nick = null;
    private Set<Channel> _channels;

    public ChatUser( Socket socket, String nick) {
        _socket = socket;
        _nick = nick;
        _channels = Collections.synchronizedSet( new HashSet<Channel>() );
    }

    public Socket getSocket() { return _socket; }
    public void setSocket( Socket socket ) { _socket = socket; }

    public String getNick() { return _nick; }
    public void setNick( String nick ) { _nick = nick; }

    public Set<Channel> getChannels() {
        Log.debug( "Requesting user: " + _nick + "'s channels" );
        return _channels;
    }
    public boolean addChannel( Channel channel ) { return _channels.add( channel ); }
    public boolean removeChannel( Channel channel ) { return _channels.remove( channel ); }

    public String toString() {
        return "ChatUser object"
            + "\n\tnick: " + _nick
            + "\n\tsocket: " + _socket
            + "\n\tchannels: " + _channels;
    }
}
