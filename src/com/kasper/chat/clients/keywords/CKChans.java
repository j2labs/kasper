package com.kasper.chat.clients.keywords;

import com.kasper.chat.clients.ChatClient;

public class CKChans extends ChatKeyword {

    public CKChans( ChatClient chatClient, String keywordId ) {
        super( chatClient, keywordId);
    }

    public void processKeyword( String nick, boolean sending, String arguments ) {
        _chatClient.sendChannelListRequestMessage();
    }

    public String helpDesc() {
        return "gets a list of all active channels";
    }
}
