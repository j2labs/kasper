
package com.kasper.ms;

public class ChatMessageConstants extends MessageConstants {
    /* A default channel used if client has no auto-join channels */
    final public static String FALLBACK_CHANNEL = "whatevs";
    final public static String ALL_CHANNELS = "ALL";

    /* constants that appear in chat messages */
    public static String NICK_IS_NOW = " is now ";

    /* chat message type */
    final public static MessageConstant CONVERSATION =
        new MessageConstant( 0, "CONVERSATION", "Conversation message" );
    
    final public static MessageConstant PRIV_MSG =
        new MessageConstant( 1, "PRIV_MSG", "Private message" );

    final public static MessageConstant CONNECT =
        new MessageConstant( 2, "CONNECT", "Connection" );

    final public static MessageConstant DISCONNECT =
        new MessageConstant( 3, "DISCONNECT", "Disconnection" );

    final public static MessageConstant NICK_CHANGED =
        new MessageConstant( 4, "NICK_CHANGED", "Nick changed" );

    final public static MessageConstant NICK_LIST =
        new MessageConstant( 5, "NICK_LIST", "List of nicks in channel" );

    final public static MessageConstant JOIN_CHANNEL =
        new MessageConstant( 6, "JOIN_CHANNEL", "Joining a channel" );

    final public static MessageConstant SUCCESSFULLY_JOINED_CHANNEL =
        new MessageConstant( 7, "SUCCESSFULLY_JOINED_CHANNEL", "Joined channel" );

    final public static MessageConstant ERROR_JOINING_CHANNEL =
        new MessageConstant( 8, "ERROR_JOINING_CHANNEL", "Failed to join channel" );

    final public static MessageConstant LEAVE_CHANNEL =
        new MessageConstant( 9, "LEAVE_CHANNEL", "Leaving a channel" );

    final public static MessageConstant LEFT_CHANNEL =
        new MessageConstant( 10, "LEFT_CHANNEL", "Left channel" );

    final public static MessageConstant CHANNEL_LIST =
        new MessageConstant( 11, "CHANNEL_LIST", "List of active channels" );

    final public static MessageConstant REQUEST_CHANNEL_LIST =
        new MessageConstant( 12, "REQUEST_CHANNEL_LIST", "Requesting list of active channels" );

    final public static MessageConstant SHARED_KEYWORD =
        new MessageConstant( 13, "SHARED_KEYWORD", "Functionality triggered by one user that affects multiple" );

    final public static MessageConstant NICK_ALREADY_TAKEN =
        new MessageConstant( 14, "NICK_ALREADY_TAKEN", "Nick already in use!" );

    final public static MessageConstant CONNECTION_FAILED =
        new MessageConstant( 15, "CONNECTION_FAILED", "Connection failed to initialize" );
}
