package com.kasper.chat.engine;


public class Channel {

    private String _channelName = null;

    public Channel( String channelName ) { _channelName = channelName; }

    public String getChannelName() { return _channelName; }

    public int hashCode() {
        return _channelName.hashCode();
    }

    public String toString() {
        return getChannelName();
    }
}
