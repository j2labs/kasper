package com.kasper.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

import com.kasper.core.Log;
import com.kasper.ms.Message;
import com.kasper.ms.MessageContainer;

public class Server {

    private Service _service;
    private int _port;
    private ServerSocket _ss;
    private Hashtable<Socket, DataOutputStream> _outputStreams = new Hashtable<Socket, DataOutputStream>();
    
    public Server( Service service, int port ) throws IOException {
        _service = service;
        _port = port;
    }

    public void listen() throws IOException {
        _ss = new ServerSocket( _port );

        Log.info( "Listening on " + _ss );

        while( true ) {
            Socket s = _ss.accept();
            Log.info( "Connection opened: " + s );

            DataOutputStream dout = new DataOutputStream( s.getOutputStream() );
            _outputStreams.put( s, dout );

            _service.connecting( s );

            new ServerThread( this, s );
        }
    }

    public void receivedMessage( Socket socket, Message message ) {
        _service.processMessage( socket, message );
    }

    private Enumeration<DataOutputStream> getOutputStreams() {
        return _outputStreams.elements();
    }

    protected void removeConnection( Socket s ) {
        synchronized( _outputStreams ) {
            _outputStreams.remove( s );

            _service.disconnecting( s );

            try {
                Log.info( "Closing connection: " + s );
                s.close();
            }
            catch( IOException ie ) {
                Log.error( "Error closing: " + s );
                Log.error( ie );
            }
        }
    }

    public void sendTo( Socket s, Message message ) {
        DataOutputStream dout = _outputStreams.get( s );

        try {
            MessageContainer mc = new MessageContainer( message );
            mc.writeStream( dout );
        }
        catch( IOException ie ) {
            Log.error( ie );
        }
    }

    public void sendToAll( Message message ) {
        Log.info("Sending message: " + message);

        synchronized( _outputStreams ) {
            for (Enumeration<DataOutputStream> e = getOutputStreams(); e.hasMoreElements(); ) {
                DataOutputStream dout = (DataOutputStream) e.nextElement();

                try {
                    MessageContainer mc = new MessageContainer( message );
                    mc.writeStream( dout );
                }
                catch( IOException ie ) {
                    Log.error( ie );
                }
            }
        }
    }
}
