package com.kasper.chat.client.keywords;

import com.kasper.chat.client.ChatClient;

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
