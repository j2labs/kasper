package com.kasper.chat.client.keywords;

import com.kasper.chat.client.ChatClient;

public class CKLeave extends ChatKeyword {

    public CKLeave( ChatClient chatClient, String keywordId ) {
        super( chatClient, keywordId);
    }

    public void processKeyword( String nick, boolean sending, String arguments ) {
        _chatClient.sendLeaveChannelMessage( arguments );
    }

    public String helpDesc() {
        return "leaves a channel where the channel name is " + ARGUMENTS;
    }
}
