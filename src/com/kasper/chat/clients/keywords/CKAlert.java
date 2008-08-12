package com.kasper.chat.clients.keywords;

import com.kasper.chat.clients.ChatClient;
import com.kasper.core.Log;

public class CKAlert extends ChatKeyword {

    public CKAlert( ChatClient chatClient, String keywordId ) {
        super( chatClient, keywordId, true );
    }

    public void processKeyword( String nick, boolean sending, String arguments ) {
        if( !sending && !nick.equals(  _chatClient.getChatConfig().getNick() )) {
            Log.debug( "Triggering alert: " + arguments );
            _chatClient.alertUser( arguments );
        }
        else if( sending ) {
            _chatClient.sendKeywordMessage( arguments );
        }
    }

    public String helpDesc() {
        return "alerts all users in a channel with " + ARGUMENTS;
    }
}
