package com.kasper.chat.engine;

import java.net.Socket;
import java.lang.String;
import java.util.Collections;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import com.kasper.core.Log;

public class UserChannelMap {

    // Any return values >= 0 signal success
    public static int SUCCESS = 0;
    public static int SUCCESS_CHANNEL_REMOVED = 1;

    // Any return values < 0 signal error
    public static int ERROR = -1;
    public static int USER_ALREADY_IN_CHANNEL = -2;
    public static int USER_DOES_NOT_EXIST = -3;
    public static int USER_NICK_ALREADY_TAKEN = -4;
    public static int CHANNEL_DOES_NOT_EXIST = -5;

    private Set<ChatUser> _users
        = Collections.synchronizedSet( new HashSet<ChatUser>() );
    private Map<Channel, Set<ChatUser>> _channelToUsers
        = Collections.synchronizedMap( new Hashtable<Channel, Set<ChatUser>>() );

    public ChatUser getUser( String nick ) {
        Log.debug( "Looking for " + nick + " in list of " + _users.size() + " users." );

        ChatUser userFound = null;
        synchronized( _users ) {
            Iterator<ChatUser> i = _users.iterator();
            while( i.hasNext() ) {
                ChatUser user = (ChatUser) i.next();
                Log.debug( "Looking for " + nick + " - " + user.getNick() );
                if( user.getNick().equals( nick ) ) {
                    userFound = user;
                    break;
                }
            }
        }

        return userFound;
    }

    public ChatUser getUser( Socket socket ) {
        Log.debug( "Looking for socket: " + socket + " in list of " + _users.size() + " users." );

        ChatUser userFound = null;
        synchronized( _users ) {
            Iterator<ChatUser> i = _users.iterator();
            while( i.hasNext() ) {
                ChatUser user = (ChatUser) i.next();
                Log.debug( "Looking for " + socket + " - " + user.getSocket() );
                if( user.getSocket().equals( socket ) ) {
                    userFound = user;
                    break;
                }
            }
        }
        
        return userFound;
    }

    public Channel getChannel( String channelName ) {
        Channel foundChannel = null;
        Set<Channel> channels = _channelToUsers.keySet();
        Log.debug( "Looking for " + channelName + " in " + channels.size() + " channels" );
        synchronized( _channelToUsers ) {
            Iterator<Channel> i = channels.iterator();
            while( i.hasNext() ) {
                Channel chan = (Channel) i.next();
                Log.debug( "Looking for " + channelName + " - " + chan.getChannelName() );
                if( chan.getChannelName().equals( channelName ) ) {
                    foundChannel = chan;
                    break;
                }
            }
            return foundChannel;
        }
    }

    public int getChannelSize( Channel c ) {
        Set<ChatUser> channelUsers = getChannelUsers( c.getChannelName() );
        if( channelUsers == null) {
            return CHANNEL_DOES_NOT_EXIST;
        }
        return channelUsers.size();
    }

    public Set<Channel> getActiveChannels() {
        return _channelToUsers.keySet();
    }

    public Set<ChatUser> getChannelUsers( String channelName ) {
        Log.debug( "Fetching users for channel: " + channelName );
        Channel channel = getChannel( channelName );
        if( channel == null ) {
            return null;
        }
        return _channelToUsers.get( channel );
    }

    public int addUser( ChatUser user ) {
        Log.debug( "Adding user: " + user.getNick() );
        if( getUser( user.getNick() ) != null ) {
            return USER_NICK_ALREADY_TAKEN;
        }
        
        _users.add( user );
        return SUCCESS;
    }

    // returns channels user *was* a member of that still have size > 0
    public Set<Channel> removeUser( ChatUser user ) {
        Log.debug( "Removing user from server: " + user );

        Set<Channel> wasInChannels = new HashSet<Channel>();
        _users.remove( user ); // collection is synchronized
        
        Set<Channel> userChannels = user.getChannels();
        synchronized( userChannels ) {
            Iterator<Channel> i = userChannels.iterator();
            while( i.hasNext() ) {
                Channel channel = (Channel) i.next();
                removeUserFromChannel( user, channel, false );
                if( getChannelSize( channel ) > 0 ) {
                    wasInChannels.add( channel );
                }
            }
        }

        return wasInChannels;
    }

    public int changeUsersNick( String oldNick, String newNick ) {
        if( oldNick.equals( newNick ) ) {
            return SUCCESS;
        }
        else if( getUser( newNick ) != null ) {
            return USER_NICK_ALREADY_TAKEN;
        }

        ChatUser user = getUser( oldNick );
        user.setNick( newNick );
        return SUCCESS;
    }

    public void addChannel( Channel channel ) {
        Log.debug( "Adding channel: " + channel.getChannelName() );
        
        Set<ChatUser> users
            = Collections.synchronizedSet( new HashSet<ChatUser>() );
        
        _channelToUsers.put( channel, users );
    }
    
    private void removeChannel( Channel channel ) {
        Log.debug( "Removing channel: " + channel.getChannelName() );
        _channelToUsers.remove( channel );
    }

    public int addUserToChannel( String user, String channel ) {
        ChatUser userObj = getUser( user );
        Channel channelObj = getChannel( channel );
        if( userObj == null ) {
            Log.error( "Attempting to add invalid user: " + user + " to channel: " + channel );
            return USER_DOES_NOT_EXIST;
        }
        
        if( channelObj == null ) {
            channelObj = new Channel( channel );
        }

        return addUserToChannel( userObj, channelObj );
    }

    public int addUserToChannel( ChatUser user, Channel channel ) {
        int status = ERROR;
        Log.debug("Attempting add " + user.getNick() + " to " + channel.getChannelName() + ".");
        if( !_channelToUsers.containsKey( channel ) ) {
            Log.debug( "Creating new channel: " + channel );
            addChannel( channel );
        }
        
        // Add user to users in channel
        Set<ChatUser> usersInChannel = _channelToUsers.get( channel );
        if( usersInChannel.contains( user ) ) {
            Log.error( "User : " + user.getNick() + " already in " + channel.getChannelName() );
            return USER_ALREADY_IN_CHANNEL;
        }
        else {
            Log.debug( "User : " + user.getNick() + " added to " + channel.getChannelName() );
            usersInChannel.add( user );
            status = SUCCESS; // change later if problem occurs
        }

        // Add channel to user's channel list
        Log.info( "Adding " + channel.getChannelName() + " to " + user.getNick() + "'s channel list");
        user.addChannel( channel );

        return status;
    }

    public int removeUserFromChannel( String user, String channel ) {
        return removeUserFromChannel( user, channel, true );
    }

    public int removeUserFromChannel( String user, String channel, boolean inChannelIteration ) {
        ChatUser userObj = getUser( user );
        Channel channelObj = getChannel( channel );

        return removeUserFromChannel( userObj, channelObj, inChannelIteration );
    }

    public int removeUserFromChannel( ChatUser user, Channel channel, boolean inChannelIteration ) {
        //int status = ERROR;
        Log.debug("Attempting to remove " + user.getNick() + " from " + channel.getChannelName() + ".");
                
        // remove user from channel
        Set<ChatUser> usersInChannel = _channelToUsers.get( channel );
        usersInChannel.remove( user );

        // remove channel from user
        if( inChannelIteration ) {
            user.removeChannel( channel );
        }
        
        Set<ChatUser> chanUsers = _channelToUsers.get( channel );
        chanUsers.remove( user );

        if( chanUsers.size() < 1 ) {
            Log.info( "Removing channel: " + channel.getChannelName() + ".");
            removeChannel( channel );
            return SUCCESS_CHANNEL_REMOVED;
        }

        return SUCCESS;
    }

}
