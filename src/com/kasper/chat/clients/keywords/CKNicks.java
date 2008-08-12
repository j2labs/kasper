package com.kasper.chat.clients.keywords;

import com.kasper.chat.clients.ChatClient;

public class CKNicks extends ChatKeyword {

    public CKNicks( ChatClient chatClient, String keywordId ) {
        super( chatClient, keywordId);
    }

    public void processKeyword( String nick, boolean sending, String arguments ) {
        _chatClient.sendNickListRequestMessage();
    }

    public String helpDesc() {
        return "gets a list of all active channels";
    }
}
