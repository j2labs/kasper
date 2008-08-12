package com.kasper.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.kasper.chat.config.ChatConfig;
import com.kasper.core.EnvProperties;
import com.kasper.core.EnvPropertyKeys;
import com.kasper.core.Log;
import com.kasper.ms.ChatMessage;
import com.kasper.ms.ChatMessageConstants;
import com.kasper.ms.Message;
import com.kasper.ms.MessageContainer;
import com.kasper.service.Service;


public class ChatClient extends Service {

    private final static String ARG_CHAT_SERVER_HOST = ARG_PREFIX + "chat-host";
    private final static String ARG_CHAT_SERVER_PORT = ARG_PREFIX + "chat-port";

    private ClientWindow _clientWindow = null;
    private ChatConfig _chatConfig = null;
    private ChatKeywordHandler _chatKeywordHandler = null;

    public static String ALL_CHANNELS = ChatMessageConstants.ALL_CHANNELS;

    protected Socket _socket = null;
    protected DataOutputStream _dout = null;
	protected DataInputStream _din = null;

    private boolean _connected = false;

    public String getServiceName() {
        return "Kasper Chat Client";
    }

    public String getServiceVersion() {
        return "0.7";
    }

    public void initServiceConfig() {
        _chatConfig = new ChatConfig( this );
        setServiceConfig( _chatConfig );
    }

    public void additionalHelpInfo( StringBuffer help ) {
        help.append( ARG_CHAT_SERVER_HOST + " <hostname> host chat server is on.\n" );
        help.append( ARG_CHAT_SERVER_PORT + " <port> port chat server is on." );
    }

    public void initServiceLogic() {
        try {
            UIManager.setLookAndFeel( _chatConfig.getLookAndFeel() );
        } catch(Exception e) {
            System.out.println("Error setting native LAF: " + e);
        }

        String nick = promptUserForNick( "What should we call you?" );

        _chatKeywordHandler = new ChatKeywordHandler( this );
        _clientWindow = new ClientWindow( this, nick );

        connectToServer();
    }

    protected void connectToServer() {
        try {
            _socket = new Socket( _chatConfig.getChatServerHostname(), _chatConfig.getChatServerPort() );
            
            Log.info( "Connected successfully to host:" + _chatConfig.getChatServerHostname()
                      + " on port:" + _chatConfig.getChatServerPort() );
            
            _din = new DataInputStream( _socket.getInputStream() );
            _dout = new DataOutputStream( _socket.getOutputStream() );
            
            new Thread( new ListenerThread() ).start();
            
            connecting( _socket );
        }
        catch( IOException ioe ) {
            Log.critical( ioe );
            connectionFailed();
        }
    }

    public void additionalProperties() {
        String chatHost = EnvProperties.getProperty( EnvPropertyKeys.CHAT_RMI_HOST );
        int chatPort = new Integer( EnvProperties.getProperty( EnvPropertyKeys.CHAT_RMI_PORT ) );
        String tabPlacement = EnvProperties.getProperty( EnvPropertyKeys.TAB_PLACEMENT );
        String clientFgColor = EnvProperties.getProperty( EnvPropertyKeys.CLIENT_FG_COLOR );
        String clientBgColor = EnvProperties.getProperty( EnvPropertyKeys.CLIENT_BG_COLOR );
        String clientNickColor = EnvProperties.getProperty( EnvPropertyKeys.CLIENT_NICK_COLOR );
        String channels = EnvProperties.getProperty(EnvPropertyKeys.AUTO_JOIN_CHANNELS);
        String lookAndFeel = EnvProperties.getProperty(EnvPropertyKeys.LOOK_AND_FEEL);
        String[] autoJoinChannels = null;
        if(channels != null) {
            autoJoinChannels = channels.split(",");
        }
        else {
            autoJoinChannels = new String[]{ChatMessageConstants.FALLBACK_CHANNEL};
        }
        
        _chatConfig.setChatServerHostname( chatHost );
        _chatConfig.setChatServerPort( chatPort );
        _chatConfig.setTabPlacement( tabPlacement );
        _chatConfig.setAutoJoinChannels( autoJoinChannels );
        _chatConfig.setLookAndFeel( lookAndFeel );
        
        if( clientFgColor != null )
            _chatConfig.setFgColor( clientFgColor );
        if( clientBgColor != null )
            _chatConfig.setBgColor( clientBgColor );
        if( clientNickColor != null )
            _chatConfig.setNickColor( clientNickColor );
    }

    public void additionalCmdLineArgs( Map<String,String> arguments ) {
        String chatHost = arguments.get( ARG_CHAT_SERVER_HOST );
        String chatPort = arguments.get( ARG_CHAT_SERVER_PORT );

        if( chatHost != null) _chatConfig.setChatServerHostname( chatHost );
        if( chatPort != null) _chatConfig.setChatServerPort( new Integer( chatPort ) );
    }

    public void processMessage( Socket socket, Message message ) {
        ChatMessage chatMessage = (ChatMessage) message;

        int type = chatMessage.getChatMsgType();
        String nick = chatMessage.getNick();
        String text = chatMessage.getChatText();
        String channel = chatMessage.getChannel();

        boolean displayMessage = true;

        Log.debug( "Received msg: " + chatMessage );
        Log.debug( "Type recvd: " + type );
        
        if( type == ChatMessageConstants.CONNECT.code() ) {
            Log.debug( "SUCCESSFULLY_CONNECTED" );
            if( ! _connected ) {
                autoJoinChannels();
            }
            _connected = true;
        }
        else if( type == ChatMessageConstants.SUCCESSFULLY_JOINED_CHANNEL.code() ) {
            Log.debug( "SUCCESSFULLY_JOINED" );
            if( text.equals( _chatConfig.getNick() ) ) {
                displayMessage = joinedChannel( channel );
            }
        }
        else if( type == ChatMessageConstants.LEFT_CHANNEL.code() ) {
            Log.debug( "LEFT_CHANNEL" );
            if( text.equals( _chatConfig.getNick() ) ) {
                leftChannel( channel );
                displayMessage = false; // they know because a tab disappears
            }
        }
        else if( type == ChatMessageConstants.ERROR_JOINING_CHANNEL.code() ) {
            displayMessage = errorJoiningChannel( channel );
        }
        else if( type == ChatMessageConstants.NICK_CHANGED.code() ) {
            if( nick.equals( _chatConfig.getNick() ) ) {
                _chatConfig.setNick( text );
            }
            text = nick + ChatMessageConstants.NICK_IS_NOW + text;
            nick = ChatMessageConstants.NICK_CHANGED.desc();
        }
        else if( type == ChatMessageConstants.NICK_ALREADY_TAKEN.code() ) {
            if( _connected == false ) {
                promptUserForNick( "Nick choice taken already.\n"
                                   + "Please make a new choice" );
                connecting( _socket );
            }
            else {
                text = "Please pick one other than: " + text;
            }
        }
        else if( type == ChatMessageConstants.CONNECTION_FAILED.code() ) {
            connectionFailed();
        }
        else if( type == ChatMessageConstants.CONVERSATION.code() ) {
            displayMessage = handleKeyword( nick, text, false );
        }

        if( displayMessage ) {
            displayMessage( channel, nick, text );
        }
    }

    public void connectionFailed() {
        _connected = false;
        int response = _clientWindow.askUser( "Connection failed. Try again?" );

        if(response == 0) {
            connectToServer();
        }
        else {
            displayMessage( ALL_CHANNELS, "CONNECTION TERMINATED", "[ fin ]" );
        }
    }

    public void connecting( Socket socket ) {
        sendMessage( ChatMessageConstants.CONNECT.code(), _chatConfig.getNick() );
    }

    public void disconnecting( Socket socket ) {
        _connected = false;
        sendMessage( ChatMessageConstants.DISCONNECT.code(), _chatConfig.getNick() );
    }
    
    public void disconnect() {
        disconnecting( _socket );
    }

    public boolean joinedChannel( String channel ) {
        Log.debug( "Joined channel: " + channel );
        boolean returnVal =  _chatConfig.addChannel( channel );
        returnVal = _clientWindow.createChannelTab( channel );
        return returnVal;
    }

    public boolean leftChannel( String channel ) {
        Log.debug( "Left channel: " + channel );
        boolean returnVal =  _chatConfig.removeChannel( channel );
        returnVal = _clientWindow.removeChannelTab( channel );
        return returnVal;
    }

    public void autoJoinChannels() {
        for(String channel : _chatConfig.getAutoJoinChannels()) {
            sendJoinChannelMessage( channel );
        }
    }

    public boolean errorJoiningChannel( String channel ) {
        // nothing yet
        return true;
    }

    public boolean leaveChannel( String channel ) {
        Log.debug( "Leaving channel: " + channel );
        return _chatConfig.removeChannel( channel );
    }

    public void sendJoinChannelMessage( String channel ) {
        sendMessage( ChatMessageConstants.JOIN_CHANNEL.code(), channel, channel );
    }

    public void sendLeaveChannelMessage( String channel ) {
        sendMessage( ChatMessageConstants.LEAVE_CHANNEL.code(), channel, channel );
    }

    public void sendChannelListRequestMessage() {
        sendMessage( ChatMessageConstants.REQUEST_CHANNEL_LIST.code() );
    }

    public void sendNickChangeRequestMessage( String newNick ) {
        sendMessage( ChatMessageConstants.NICK_CHANGED.code(), newNick);
    }

    public void sendNickListRequestMessage() {
        sendMessage( ChatMessageConstants.NICK_LIST.code(), _chatConfig.getNick() );
    }

    public void sendKeywordMessage( String text ) {
        sendMessage( ChatMessageConstants.SHARED_KEYWORD.code(), text );
    }

    public void sendChatMessage( String message ) {
        sendMessage( ChatMessageConstants.CONVERSATION.code(), message );
    }

    private void sendMessage( int chatMsgType ) {
        sendMessage( chatMsgType, _chatConfig.getNick(), _chatConfig.getCurrentChannel() );
    }
        
	private void sendMessage( int chatMsgType, String msgText ) {
        sendMessage( chatMsgType, msgText, _chatConfig.getCurrentChannel() );
    }

	private void sendMessage( int chatMsgType, String msgText, String channel ) {
        ChatMessage msg = new ChatMessage( 1,
                                           chatMsgType,
                                           _chatConfig.getNick(),
                                           msgText,
                                           channel );
        
        Log.debug("Sending message type: " + chatMsgType);
        Log.debug("Sending message : " + msgText);

		try {
            MessageContainer mc = new MessageContainer( msg );
            mc.writeStream( _dout );
		}
		catch( IOException ie ) {
			Log.error( ie );
		}
	}

    public void setChatConfig( ChatConfig chatConfig ) { _chatConfig = chatConfig; }
	public ChatConfig getChatConfig() { return _chatConfig; }

    public void setCurrentClientPanel( int idx ) { _clientWindow.setCurrentClientPanel( idx ); }
    public ClientPanel getCurrentClientPanel() { return _clientWindow.getCurrentClientPanel(); }
    public ClientPanel getClientPanel( String title ) { return _clientWindow.findClientPanel( title ); }

    public boolean handleKeyword( String nick, String message, boolean sending ) {
        return _chatKeywordHandler.handleKeyword( nick, message, sending );
    }

    public void clearConvoText() {
        _clientWindow.getCurrentClientPanel().clearConvoText();
    }

    public void alertUser( String alert ) {
        _clientWindow.alertUser( alert );
    }

    public void displayMessage( String label, String nick, String text ) {
        _clientWindow.displayMessage( label, nick, text );
    }

    public String promptUserForNick( String prompt ) {
        String nick = null;

        do {
            nick = JOptionPane.showInputDialog( null, prompt + "\n\n(type 'exit' to quit)  " );
            if( nick.equals("exit") ) {
                systemExit();
            }
        } while ( nick == null || nick.trim().length() == 0 );

        _chatConfig.setNick( nick );

        return nick;
    }

    class ListenerThread implements Runnable {
        public void run() {
            try {
                while (true) {
                    MessageContainer mc = new MessageContainer();
                    mc.readStream( _din );
                    processMessage( _socket, mc.getMessage() );
                }
            }
            catch( IOException ie ) {
                Log.error( ie );
                connectionFailed();
            }
            catch( ClassNotFoundException e ) {
                Log.error( e );
            }
        }
    }

	public static void main(String[] args) {
        ChatClient client = new ChatClient();

        client.initService( args );
	}
}
