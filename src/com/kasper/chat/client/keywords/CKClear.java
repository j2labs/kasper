package com.kasper.chat.client.keywords;

import com.kasper.chat.client.ChatClient;

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
