package com.kasper.chat.clients.keywords;

import com.kasper.chat.clients.ChatClient;

public class CKClear extends ChatKeyword {

    public CKClear( ChatClient chatClient, String keywordId ) {
        super( chatClient, keywordId);
    }

    public void processKeyword( String nick, boolean sending, String arguments ) {
        _chatClient.clearConvoText();
        System.gc();
    }

    public String helpDesc() {
        return "clears the conversation pane";
    }
}
