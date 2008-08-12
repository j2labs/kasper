package com.kasper.chat.clients;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import com.kasper.core.Log;
import com.kasper.chat.clients.keywords.CKAlert;
import com.kasper.chat.clients.keywords.CKChans;
import com.kasper.chat.clients.keywords.CKClear;
import com.kasper.chat.clients.keywords.CKJoin;
import com.kasper.chat.clients.keywords.CKLeave;
import com.kasper.chat.clients.keywords.CKNick;
import com.kasper.chat.clients.keywords.CKNicks;
import com.kasper.chat.clients.keywords.ChatKeyword;


public class ChatKeywordHandler {

	private static enum KEYWORDS {
        fontsize, fontface, fg, bg, nickcolor,
        tab, t
    }

    ChatClient _chatClient = null;
    private Map<String, ChatKeyword> keywordMap = new HashMap<String, ChatKeyword>();
    private String KEYWORD_PREFIX = ChatKeyword.KEYWORD_PREFIX; // TODO delete this

    public static String CKClear = ChatKeyword.KEYWORD_PREFIX + "clear";
    public static String CKNick = ChatKeyword.KEYWORD_PREFIX + "nick";
    public static String CKJoin = ChatKeyword.KEYWORD_PREFIX + "join";
    public static String CKLeave = ChatKeyword.KEYWORD_PREFIX + "leave";
    public static String CKChans = ChatKeyword.KEYWORD_PREFIX + "chans";
    public static String CKNicks = ChatKeyword.KEYWORD_PREFIX + "nicks";
    public static String CKAlert = ChatKeyword.KEYWORD_PREFIX + "alert";
    
    public ChatKeywordHandler( ChatClient chatClient ) {
        _chatClient = chatClient;
        initKeywords();
    }

    public void initKeywords() {
        ChatKeyword ckalert = new CKAlert( _chatClient, CKAlert );
        keywordMap.put( CKAlert, ckalert );

        ChatKeyword ckchans = new CKChans( _chatClient, CKChans );
        keywordMap.put( CKChans, ckchans );

        ChatKeyword ckclear = new CKClear( _chatClient, CKClear );
        keywordMap.put( CKClear, ckclear );

        ChatKeyword ckjoin = new CKJoin( _chatClient, CKJoin );
        keywordMap.put( CKJoin, ckjoin );

        ChatKeyword ckleave = new CKLeave( _chatClient, CKLeave );
        keywordMap.put( CKLeave, ckleave );

        ChatKeyword cknick = new CKNick( _chatClient, CKNick );
        keywordMap.put( CKNick, cknick );

        ChatKeyword cknicks = new CKNicks( _chatClient, CKNicks );
        keywordMap.put( CKNicks, cknicks );
    }

    public boolean handleKeyword( String nick, String message, boolean sending ) {
        
        // message requires processing after keyword processing
        boolean additionalProcessing = false;

        String values[] = message.split(" ", 2);
        boolean hasArgs = (values.length > 1);

        if( !message.startsWith( KEYWORD_PREFIX ) ) {
            return true;
        }
        
        String possibleKeyword = values[0];
        String arguments = null;
        if( hasArgs ) arguments = values[1];

        if( values.length < 1 ) return false; // empty str

        Log.debug( "Inspect message for keyword: " + possibleKeyword );

		if( keywordMap.containsKey( possibleKeyword ) ) {
            ChatKeyword ck = keywordMap.get( possibleKeyword );
            ck.processKeyword( nick, sending, arguments );
            additionalProcessing = ck.getAdditionalProcessing();
		}
        else if( possibleKeyword.equals( KEYWORD_PREFIX + KEYWORDS.fontsize ) ) {
            if( hasArgs ) {
                _chatClient.getChatConfig().setFontSize( new Integer( arguments ) );
            }
        }
        else if( possibleKeyword.equals( KEYWORD_PREFIX + KEYWORDS.fontface ) ) {
            if( hasArgs ) {
                _chatClient.getChatConfig().setFontFace( arguments );
            }
        }
        else if( possibleKeyword.equals( KEYWORD_PREFIX + KEYWORDS.fg ) ) {
            if( hasArgs ) {
                try {
                    _chatClient.getChatConfig().setFgColor( arguments );
                }
                catch( NumberFormatException nfe ) {
                    String error = "Foreground color argument is not valid!";
                    Log.error( error );
                    _chatClient.displayMessage( _chatClient.getChatConfig().getCurrentChannel(),
                                                "Display", error );
                }

            }
        }
        else if( possibleKeyword.equals( KEYWORD_PREFIX + KEYWORDS.bg ) ) {
            if( hasArgs ) {
                try {
                    _chatClient.getChatConfig().setBgColor( arguments );
                }
                catch( NumberFormatException nfe ) {
                    String error = "Background color argument is not valid!";
                    Log.error( error );
                    _chatClient.displayMessage( _chatClient.getChatConfig().getCurrentChannel(),
                                                "Display", error );
                }
            }
        }
        else if( possibleKeyword.equals( KEYWORD_PREFIX + KEYWORDS.nickcolor ) ) {
            if( hasArgs ) {
                _chatClient.getChatConfig().setNickColor( arguments );
            }
        }
        else if( possibleKeyword.equals( KEYWORD_PREFIX + KEYWORDS.tab )||
                 possibleKeyword.equals( KEYWORD_PREFIX + KEYWORDS.t ) ) {
            if( hasArgs ) {
                try {
                    Integer tab = new Integer( arguments );
                    _chatClient.setCurrentClientPanel( tab.intValue() );
                } catch (NumberFormatException nfe) {}
            }
        }
        // Not a keyword!
        else {
            additionalProcessing = true;
        }

        return additionalProcessing;
	}
}
