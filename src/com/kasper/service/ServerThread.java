package com.kasper.service;

import java.io.DataInputStream;
import java.net.Socket;
import java.net.SocketException;

import com.kasper.core.Log;
import com.kasper.ms.Message;
import com.kasper.ms.MessageContainer;

public class ServerThread extends Thread {

    private Server _server;
    private Socket _socket;

    public ServerThread( Server server, Socket socket ) {
        _server = server;
        _socket = socket;

        start();
    }

    public void run() {
        try {
            DataInputStream din = new DataInputStream( _socket.getInputStream() );

            while (true) {
                Message message = new Message();

                try {
                    MessageContainer workingMsg = new MessageContainer();
                    workingMsg.readStream( din );
                    message = workingMsg.getMessage();

                    _server.receivedMessage( _socket, message );
                }
                catch( ClassNotFoundException e ) {
                    Log.error( e );
                }
                finally {
                    message = null;
                }
            }
        }
        catch( SocketException se ) {
            Log.info( "Connection broken: " + se );
            _server.removeConnection( _socket );
        }
        catch( Exception ie ) {
            Log.error( ie );
        }
    }
}
