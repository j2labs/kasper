package com.kasper.chat.client.keywords;

import com.kasper.chat.client.ChatClient;

public abstract class ChatKeyword {

    public static String KEYWORD_PREFIX = "/";
    public static String ARGUMENTS = "<arguments>";

    protected ChatClient _chatClient = null;
    private String _keywordId = null;
    private boolean _additionalProcessing = false;

    public ChatKeyword( ChatClient chatClient, String keywordId ) {
        this(chatClient, keywordId, false);
    }
    
    public ChatKeyword( ChatClient chatClient, String keywordId, boolean additionalProcessing ) {
        _chatClient = chatClient;
        _keywordId = keywordId;
        _additionalProcessing = additionalProcessing;
    }

    public abstract void processKeyword( String nick, boolean sending, String arguments );
    public abstract String helpDesc();

    public boolean getAdditionalProcessing() { return _additionalProcessing; }

    public String getKeywordId() { return _keywordId; }

    public String help() {
        return KEYWORD_PREFIX + getKeywordId() + ARGUMENTS + ": " + helpDesc();
    }

}
