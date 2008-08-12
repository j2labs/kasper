package com.kasper.chat.engine;

import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.kasper.core.EnvProperties;
import com.kasper.core.EnvPropertyKeys;
import com.kasper.core.Log;
import com.kasper.ms.ChatMessage;
import com.kasper.ms.ChatMessageConstants;
import com.kasper.ms.Message;
import com.kasper.service.ChatServiceConfig;
import com.kasper.service.Server;
import com.kasper.service.Service;

public class ChatEngine extends Service {

    public final static String ARG_CHAT_SERVER_HOST = ARG_PREFIX + "chat-host";
    public final static String ARG_CHAT_SERVER_PORT = ARG_PREFIX + "chat-port";

    Server _server = null;
    ChatServiceConfig _chatEngineConfig = null;

    private UserChannelMap _userChanMap = new UserChannelMap();
    
    public String DELIM = Log.LOG_DELIM;
    public String CONNECTED = "Connected";
    public String DISCONNECTED = "Disconnected";

    public String getServiceName() {
        return "Kasper Chat Engine";
    }

    public String getServiceVersion() {
        return "0.5";
    }
    
    public void initServiceConfig() {
        _chatEngineConfig = new ChatServiceConfig();
        setServiceConfig( _chatEngineConfig );
    }

    public void additionalHelpInfo( StringBuffer help ) {
        help.append( ARG_CHAT_SERVER_HOST + " <hostname> not sure!\n" );
        help.append( ARG_CHAT_SERVER_PORT + " <port> port to open server on." );
    }
    
    public void initServiceLogic() {
        String hostname = _chatEngineConfig.getChatServerHostname();
        int port = _chatEngineConfig.getChatServerPort();

        Log.info( "Starting ChatEngine on host:" + hostname + " and port:" + port );
        
        try {
            _server = new Server( this, port );
            _server.listen();
        }
        catch(Exception e) {
            Log.error( "Could not start server" );
            Log.error( e );

            return;
        }
    }

    public void additionalProperties() {
        String chatHost = EnvProperties.getProperty( EnvPropertyKeys.CHAT_RMI_HOST );
        int chatPort = new Integer( EnvProperties.getProperty( EnvPropertyKeys.CHAT_RMI_PORT ) );

        _chatEngineConfig.setChatServerHostname( chatHost );
        _chatEngineConfig.setChatServerPort( chatPort );
    }

    public void additionalCmdLineArgs( Map<String,String> arguments ) {
        String chatHost = arguments.get( ARG_CHAT_SERVER_HOST );
        String chatPort = arguments.get( ARG_CHAT_SERVER_PORT );

        if( chatHost != null) _chatEngineConfig.setChatServerHostname( chatHost );
        if( chatPort != null) _chatEngineConfig.setChatServerPort( new Integer(chatPort) );
    }

    public void processMessage( Socket socket, Message message ) {
        int type = message.getType();

        // XXX Non chat message (server?) need handling
        
        if( type == ChatMessageConstants.TYPE_CHAT.code() ) {
            boolean sendToAllUsers = false;
            
            ChatMessage chatMessage = (ChatMessage) message;

            int chatMsgType = chatMessage.getChatMsgType();
            String nick = chatMessage.getNick();
            String text = chatMessage.getChatText();
            String channel = chatMessage.getChannel();

            Log.debug("Processing message: " + chatMessage);

            if( chatMsgType == ChatMessageConstants.CONVERSATION.code() ) {
                sendToChannel( channel, chatMessage );
            }
            else if( chatMsgType == ChatMessageConstants.NICK_CHANGED.code() ) {
                int retVal = _userChanMap.changeUsersNick( nick, text );
                if( retVal == UserChannelMap.SUCCESS ) {
                    chatMessage.setChatText( text );
                    sendToUsersChannels( text, chatMessage );
                }
                else if( retVal == UserChannelMap.USER_NICK_ALREADY_TAKEN ) {
                    sendNickInUseMessage( socket, text );
                }
            }
            else if( chatMsgType == ChatMessageConstants.CONNECT.code() ) {
                int retVal = registerUser( socket, text );
                if( retVal == UserChannelMap.SUCCESS ) {
                    sendToAllUsers = true;
                    chatMessage.setNick( ChatMessageConstants.CONNECT.desc() );
                    chatMessage.setChannel( ChatMessageConstants.ALL_CHANNELS );
                }
                else if( retVal == UserChannelMap.USER_NICK_ALREADY_TAKEN ) {
                    sendNickInUseMessage( socket, text );
                }
                else {
                    sendConnectFailedResponse( socket, text );
                }
            }
            else if( chatMsgType == ChatMessageConstants.DISCONNECT.code() ) {
                disconnecting( nick ); // sends message on it's own
            }
            else if( chatMsgType == ChatMessageConstants.NICK_LIST.code() ) {
                ChatMessage nickListMsg = getNickListMessage( channel );
                _server.sendTo( socket, nickListMsg );
            }
            else if( chatMsgType == ChatMessageConstants.JOIN_CHANNEL.code() ) {
                joinChannel( nick, channel );
            }
            else if( chatMsgType == ChatMessageConstants.LEAVE_CHANNEL.code() ) {
                ChatMessage leaveResponseMsg = leaveChannel( nick, channel );
                _server.sendTo( socket, leaveResponseMsg );
            }
            else if( chatMsgType == ChatMessageConstants.REQUEST_CHANNEL_LIST.code() ) {
                ChatMessage channelListMsg = listOfActiveChannelsMessage();
                _server.sendTo( socket, channelListMsg );
            }

            if( sendToAllUsers ) {
                Log.debug( "Sending message to all users: " + chatMessage );
                _server.sendToAll( chatMessage );
            }
        }
    }

    protected ChatMessage joinChannel( String nick, String channel ) {
        Log.debug( "ChatEngine: Adding user: " + nick + " to channel: " + channel );
        int retVal = _userChanMap.addUserToChannel( nick, channel );
        
        if( retVal == UserChannelMap.SUCCESS ) {
            ChatMessage chanMsg = joinChannelSuccessMessage( nick, channel );
            sendToChannel( channel, chanMsg); // inform channel
            
            return joinChannelSuccessMessage( nick, channel );
        }
        else {
            return joinChannelErrorMessage( nick, channel );
        }
    }

    protected ChatMessage leaveChannel( String nick, String channel ) {
        Log.debug( "ChatEngine: Removing user: " + nick + " from channel: " + channel );
        int retVal = _userChanMap.removeUserFromChannel( nick, channel );

        // Greater than SUCCESS ensures user was removed, but SUCCESS
        // can have other effects like removing a channel too
        if( retVal >= UserChannelMap.SUCCESS ) {
            ChatMessage chanMsg = leaveChannelSuccessMessage( nick, channel );
            if( retVal == UserChannelMap.SUCCESS ) {
                sendToChannel( channel, chanMsg ); // inform channel
            }

            return leaveChannelSuccessMessage( nick, channel );
        }
        else {
            return joinChannelErrorMessage( nick, channel );
        }
    }

    protected ChatMessage listOfActiveChannelsMessage() {
        Log.debug( "Request for list of active channels" );
        StringBuffer list = new StringBuffer();
        Set<Channel> channels = _userChanMap.getActiveChannels();
        synchronized( channels ) {
            Iterator<Channel> it = channels.iterator();
            int i=0;
            while( it.hasNext() ) {
                Channel chan = (Channel) it.next();
                if( i++ > 0 ) { list.append(", "); }
                list.append( chan.getChannelName() );
            }
        }
        
        ChatMessage message = new ChatMessage( 1,
                                               ChatMessageConstants.CHANNEL_LIST.code(),
                                               ChatMessageConstants.CHANNEL_LIST.desc(),
                                               list.toString(),
                                               "" );
        return message;
    }

    protected void sendNickInUseMessage( Socket socket, String nick ) {
        ChatMessage message = new ChatMessage( 1,
                                               ChatMessageConstants.NICK_ALREADY_TAKEN.code(),
                                               ChatMessageConstants.NICK_ALREADY_TAKEN.desc(),
                                               nick,
                                               ChatMessageConstants.ALL_CHANNELS );
        _server.sendTo( socket, message );
    }

    protected void sendConnectFailedResponse( Socket socket, String nick ) {
        ChatMessage message = new ChatMessage( 1,
                                               ChatMessageConstants.CONNECTION_FAILED.code(),
                                               ChatMessageConstants.CONNECTION_FAILED.desc(),
                                               nick,
                                               ChatMessageConstants.ALL_CHANNELS );
        _server.sendTo( socket, message );
    }

    protected ChatMessage joinChannelSuccessMessage( String nick, String channel ) {
        ChatMessage message = new ChatMessage( 1,
                                               ChatMessageConstants.SUCCESSFULLY_JOINED_CHANNEL.code(),
                                               ChatMessageConstants.SUCCESSFULLY_JOINED_CHANNEL.desc(),
                                               nick,
                                               channel );
        return message;
    }

    protected ChatMessage leaveChannelSuccessMessage( String nick, String channel ) {
        ChatMessage message = new ChatMessage( 1,
                                               ChatMessageConstants.LEFT_CHANNEL.code(),
                                               ChatMessageConstants.LEFT_CHANNEL.desc(),
                                               nick,
                                               channel );
        return message;
    }

    protected ChatMessage joinChannelErrorMessage( String nick, String channel ) {
        ChatMessage message = new ChatMessage( 1,
                                               ChatMessageConstants.ERROR_JOINING_CHANNEL.code(),
                                               ChatMessageConstants.ERROR_JOINING_CHANNEL.desc(),
                                               nick,
                                               channel );
        return message;
    }

    protected int registerUser( Socket socket, String nick ) {
        ChatUser newUser = new ChatUser( socket, nick );
        return _userChanMap.addUser( newUser );
    }

    protected ChatMessage getNickListMessage( String channelName ) {
        Log.debug( "Request for nick list message" );
        Set<ChatUser> channelUsers = _userChanMap.getChannelUsers( channelName );
        StringBuffer nicks = new StringBuffer();
        synchronized( channelUsers ) {
            Iterator<ChatUser> it = channelUsers.iterator();
            int totalNicks = channelUsers.size();
            for(int i=0; i < totalNicks; i++) {
                ChatUser user = (ChatUser) it.next();
                if(i > 0) nicks.append(", ");
                nicks.append( user.getNick() );
            }
        }
            
        ChatMessage message = new ChatMessage( 1,
                                               ChatMessageConstants.NICK_LIST.code(),
                                               ChatMessageConstants.NICK_LIST.desc(),
                                               nicks.toString(),
                                               channelName );

        return message;
    }

    // A socket connects but haven't received connect message yet
    public void connecting( Socket socket ) {
        connectLog( socket );
    }

    public void disconnecting( Socket socket ) {
        ChatUser user = _userChanMap.getUser( socket );
        Log.debug( "Disconnecting user: " + user + " with socket: " + socket );
        disconnecting( user );
    }

    public void disconnecting( String nick ) {
        ChatUser user = _userChanMap.getUser( nick );
        Log.debug( "Disconnecting user by nick: " + nick );
        disconnecting( user );
    }

    public void disconnecting( ChatUser user ) {
        Log.debug( "Disconnecting user by user object: " + user );
        Socket socket = user.getSocket();
        String nick = user.getNick();
        disconnectLog( nick, socket );

        String message = nick + " disconnected.";
        Set<Channel> userChannels = _userChanMap.removeUser( user );

        ChatMessage msg = new ChatMessage( 1,
                                           ChatMessageConstants.DISCONNECT.code(),
                                           ChatMessageConstants.DISCONNECT.desc(),
                                           message,
                                           // channel gets corrected in sendToChannel
                                           ChatMessageConstants.ALL_CHANNELS );

        if( userChannels != null ) {
            sendToChannels( userChannels, msg );
        }
	}

    public void sendToChannel( String channel, ChatMessage msg ) {
        Log.debug( "Sending message to channel: " + channel + " msg: " + msg );
        Set<ChatUser> chanUsers = _userChanMap.getChannelUsers( channel );

        // Channel doesn't exist anymore!
        if( chanUsers == null ) {
            Log.warning( "Attempted to send to non-existing channel: " + channel );
            return;
        }
        
        msg.setChannel( channel );

        synchronized( chanUsers ) {
            Iterator<ChatUser> i = chanUsers.iterator();
            while( i.hasNext() ) {
                ChatUser user = (ChatUser) i.next();
                Log.debug( "Sending message to channel user: " + user.getNick() + " msg: " + msg );
                _server.sendTo( user.getSocket(), msg );
            }
        }
    }

    public void sendToUsersChannels( String nick, ChatMessage msg ) {
        Log.debug( "Sending msg to user's channels: " + nick );
        ChatUser user = _userChanMap.getUser( nick );
        sendToChannels( user.getChannels(), msg );
    }


    public void sendToChannels( Set<Channel> channels, ChatMessage msg ) {
        if( channels == null ) {
            Log.warning( "User has no channels associated." );
        }

        Log.debug( "Sending msg to channels: " + channels );

        synchronized( channels ) {
            Iterator<Channel> i = channels.iterator();
            while( i.hasNext() ) {
                Channel channel = (Channel) i.next();
                Log.debug( "Sending msg to channel: " + channel.getChannelName() );
                sendToChannel( channel.getChannelName(), msg );
            }
        }
    }


    public void connectLog( Socket socket) {
        StringBuffer logEntry = new StringBuffer();

        logEntry.append( CONNECTED );
        logEntry.append( DELIM + socket.getInetAddress().getHostAddress() );
        logEntry.append( DELIM + socket.getPort() );

        Log.info( logEntry.toString() );
    }

    public void disconnectLog( String nick, Socket socket ) {
        StringBuffer logEntry = new StringBuffer();

        logEntry.append( nick + " " + DISCONNECTED );
        logEntry.append( DELIM + socket.getInetAddress().getHostAddress() );
        logEntry.append( DELIM + socket.getPort() );

        Log.info( logEntry.toString() );
	}

    public static void main(String args[]) throws Exception {
        ChatEngine ce = new ChatEngine();
        ce.initService( args );
    }
}
