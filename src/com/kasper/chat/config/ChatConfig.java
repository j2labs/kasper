package com.kasper.chat.config;

import java.awt.Color;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.kasper.chat.clients.ChatClient;
import com.kasper.core.Log;
import com.kasper.service.ChatServiceConfig;

public class ChatConfig extends ChatServiceConfig {
    private String _nick = "User";
    private String _fontFace = "arial";
    private int _fontSize = 3;
    private String _fgColor = "#cccccc";
    private String _bgColor = "#000000";
    private String _nickColor = "#00ff00";
    private String _lookAndFeel = "";
    private String _currentChannel = "";
    private int _tabPlacement = JTabbedPane.TOP;
    private Set<String> _activeChannels = null;
    private String[] _auto_join_channels = null;
    
    private ChatClient _chatClient = null;

    public ChatConfig( ChatClient chatClient ) {
        _chatClient = chatClient;
        _activeChannels = new HashSet<String>();
    }

    public void setNick( String nick ) { _nick = nick; }
    public String getNick() { return _nick; }
    
    public void setFontFace( String fontFace ) { _fontFace = fontFace; }
    public String getFontFace() { return _fontFace; }
    
    public void setFontSize( int fontSize ) { _fontSize = fontSize; }
    public int getFontSize() { return _fontSize; }
    
    public void setFgColor( String fgColor ) throws NumberFormatException {
        Color.decode( fgColor );
        _fgColor = fgColor;
    }
    public String getFgColor() { return _fgColor; }
    
    public void setBgColor( String bgColor ) throws NumberFormatException {
        Color.decode( bgColor );
        _bgColor = bgColor;
    }
    public String getBgColor() { return _bgColor; }
    
    public void setNickColor( String nickColor ) { _nickColor = nickColor; }
    public String getNickColor() { return _nickColor; }
    
    public boolean setCurrentChannel( String currentChannel ) {
        if( _activeChannels.contains( currentChannel ) ) {
            _currentChannel = currentChannel;
            return true;
        }
        
        return false;
    }

    public void setLookAndFeel( String lnf ) {
        if ( lnf != null && lnf.equals( "LOCAL" ) ) {
            _lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        }
        else {
            _lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
        }
    }

    public String getLookAndFeel() { return _lookAndFeel; }

    public void setAutoJoinChannels(String[] ajc) {
        _auto_join_channels = ajc;
    }

    public String[] getAutoJoinChannels() {
        return _auto_join_channels;
    }
    
    public String getCurrentChannel() { return _currentChannel; }
    public Set<String> getActiveChannels() { return _activeChannels; }
    public boolean addChannel( String channel ) { return _activeChannels.add( channel ); }
    public boolean removeChannel( String channel ) { return _activeChannels.remove( channel ); }
    
    public boolean setTabPlacement( String placement ) {
        boolean foundMatch = false;
        
        if( placement.equals( "TOP" ) ) {
            foundMatch = true;
            _tabPlacement = JTabbedPane.TOP;
        }
        else if ( placement.equals( "BOTTOM" ) ) {
            foundMatch = true;
            _tabPlacement = JTabbedPane.BOTTOM;
        }
        else if ( placement.equals( "LEFT" ) ) {
            foundMatch = true;
            _tabPlacement = JTabbedPane.LEFT;
        }
        else if ( placement.equals( "RIGHT" ) ) {
            foundMatch = true;
            _tabPlacement = JTabbedPane.RIGHT;
        }

        return foundMatch;
    }

    public int getTabPlacement() { return _tabPlacement; }

    private String getStyledText( String txt, String fgColor) {
        StringBuffer styledTxt = new StringBuffer("<font face=\"" + _fontFace + "\" size=\"" + _fontSize + "\"");
        
        if(fgColor != null) {
            styledTxt.append(" color=\"" + fgColor + "\"");
        }
        
        styledTxt.append(">" + txt + "</font>");
        
        return styledTxt.toString();
    }

    private String getStyledNick( String nick ) {
        return getStyledText( "<b>" + nick + "</b>", _nickColor );
    }

    private String getStyledMsg( String msg ) {
        return getStyledText( msg, null ); // body tag uses default fgColor
    }

    public String formatMessage( String panelTitle, String nick, String message ) {
        StringBuffer msgBuffer = new StringBuffer();

        Calendar rightNow = Calendar.getInstance();
        ConversationProps props = _chatClient.getClientPanel( panelTitle ).getConversationProps();

        boolean nickRow = ! nick.equals( props.getLastMessageFrom() );
        if( !nickRow ) {
            int hoursFromLast = props.getLastReceivedAt().get( Calendar.HOUR );
            int minutesFromLast = props.getLastReceivedAt().get( Calendar.MINUTE );

            int hoursNow = rightNow.get( Calendar.HOUR );
            int minutesRightNow = rightNow.get( Calendar.MINUTE );

            // Print new table every 5 minutes.
            if( ( minutesFromLast + 5 ) < minutesRightNow ||
                ( ( (hoursFromLast - hoursNow) != 0) &&
                  (minutesFromLast - 55 < minutesRightNow) ) )
            { 
                nickRow = true;
            }
        }

        msgBuffer.append("<table width=\"100%\">");
        
        if(nickRow) {
            String formattedDate =
                DateFormat.getDateTimeInstance( DateFormat.SHORT,
                                                DateFormat.SHORT ).format( rightNow.getTime() );
            
            msgBuffer.append( "<tr><td align=\"left\">"
                              + getStyledNick( nick ) + "</td>"
                              + "<td align=\"right\">"
                              + getStyledMsg( formattedDate ) + "</td></tr>" );
            
            props.setLastMessageFrom( nick );
            props.updateLastReceivedAt();
        }
        
        msgBuffer.append( "<tr><td colspan=\"2\">" + getStyledMsg( message ) + "</td></tr></table>" );
        Log.debug( "Formatted message: " + msgBuffer.toString() );

        return msgBuffer.toString();
    }
}
