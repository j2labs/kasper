package com.kasper.chat.client.keywords;

import com.kasper.chat.client.ChatClient;

public class CKJoin extends ChatKeyword {

    public CKJoin( ChatClient chatClient, String keywordId ) {
        super( chatClient, keywordId);
    }

    public void processKeyword( String nick, boolean sending, String arguments ) {
        _chatClient.sendJoinChannelMessage( arguments );
    }

    public String helpDesc() {
        return "joins a channel where the channel name is " + ARGUMENTS;
    }
}
