package com.kasper.chat.clients.keywords;

import com.kasper.chat.clients.ChatClient;

public class CKNick extends ChatKeyword {

    public CKNick( ChatClient chatClient, String keywordId ) {
        super( chatClient, keywordId);
    }

    public void processKeyword( String nick, boolean sending, String arguments ) {
        _chatClient.sendNickChangeRequestMessage( arguments );
    }

    public String helpDesc() {
        return "changes nickname for user";
    }
}
