package com.kasper.ms;

public class MessageConstants {

    /**************************
     * message type constants *
     **************************/
    final public static MessageConstant TYPE_NONE =
        new MessageConstant( 0, "TYPE_NONE", "No type");

    final public static MessageConstant TYPE_BROADCAST =
        new MessageConstant( 1, "TYPE_BROADCAST", "Broadcast message");

    // All custom types start from here and go up
    final public static int TYPE_START_VAL = 1000;

    public static MessageConstant TYPE_CHAT =
        new MessageConstant( TYPE_START_VAL + 1, "TYPE_CHAT", "Chat message type");
    
    /******************
     * misc constants *
     ******************/
    final public static MessageConstant WHO_NONE =
        new MessageConstant( 0, "WHO_NONE", "No who id");

}
