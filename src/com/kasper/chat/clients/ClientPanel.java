package com.kasper.chat.clients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultCaret;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.kasper.chat.config.ChatConfig;
import com.kasper.chat.config.ConversationProps;
import com.kasper.core.Log;
import com.kasper.util.BrowserControl;

public class ClientPanel extends JPanel {
    static final long serialVersionUID = -5677203147655652368L;
    
    private	ClientWindow _clientWindow;
    private ChatConfig _config;
    private ConversationProps _conversationProps;
    
    private JEditorPane _convoPane = new JEditorPane();
    private JTextField _tf = new JTextField();
    private JScrollPane _ts = new JScrollPane();
    private JScrollBar _vsb = null;
    private String _title = "";
    
    private StringBuffer _conversation = new StringBuffer();
    
    public ClientPanel( ClientWindow clientWindow, String title ) {
        
        _title = title;
        _clientWindow = clientWindow;
        _config = _clientWindow.getChatClient().getChatConfig();
        _conversationProps = new ConversationProps();
        
        _convoPane.setContentType( "text/html" );
        _convoPane.setEditable( false );
        _convoPane.addHyperlinkListener( new LinkHandler() );
        _convoPane.setBackground( Color.BLACK );
        
        // Turns off automatic scrolling
        DefaultCaret dc = (DefaultCaret) _convoPane.getCaret();
        dc.setUpdatePolicy( DefaultCaret.NEVER_UPDATE );
        
        _ts.setOpaque( true );
        _ts.getViewport().add( _convoPane );
        _vsb = _ts.getVerticalScrollBar();
        
        setLayout( new BorderLayout() );
        add( "Center", _ts );
        add( "South", _tf );
        
        _tf.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    processUserInput( e.getActionCommand() );
                }
            });
        
        addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent ce) {
                    _config.setCurrentChannel( getTitle() );
                    gainedFocus();
                }
            });
    }
    
    public void setConversationProps( ConversationProps convoProps ) { _conversationProps = convoProps; }
    public ConversationProps getConversationProps() { return _conversationProps; }
    
    public String getTitle() {
        return _title;
    }
    
    public void gainedFocus() {
        adjustTitle();
        SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    _tf.requestFocusInWindow();
                }
            });
    }
    
    private void processUserInput( String input ) {
        if( _clientWindow.getChatClient().handleKeyword( _config.getNick(), input, true ) ) {
            _conversationProps.updateLastSentAt();
            _clientWindow.getChatClient().sendChatMessage( input );
        }
        
        _tf.setText( "" );
        adjustScrollPane();
    }
    
    public boolean checkForAutoScroll() {
        int value = _vsb.getValue() + _vsb.getVisibleAmount();
        return (_vsb.getMaximum() - value) < 10;
    }
    
    public boolean adjustScrollPane() {
        boolean doWeScroll = checkForAutoScroll();
        
        if( doWeScroll ) {
            // Any adjustments to the gui need to take place
            // before adjusting the scrollbar
            SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        _vsb.setValue( _vsb.getMaximum() );
                    }
                });
        }
        
        return doWeScroll;
    }
    
    public void clearConvoText() {
    	_convoPane.setText( "" );
        _conversation = new StringBuffer();
        getConversationProps().setLastMessageFrom( "" );
    }
    
    public void adjustTitle() {
        boolean autoScroll = checkForAutoScroll();
        
        if( autoScroll && _clientWindow.getCurrentClientPanel().getTitle().equals( _title )  ) {
            _clientWindow.adjustPanelTitle( this, _title );
        }
        else {
            _clientWindow.adjustPanelTitle( this, ClientWindow.activityMarker + _title );
        }
    }
    
    public void setConvoText( String nick, String message ) {
        _clientWindow.adjustTitle();
        adjustTitle();
        _conversation.append( _config.formatMessage( _title, nick, message ) );
        
        _convoPane.setText("<html><body "
                           + "bgcolor=\"" + _config.getBgColor() + "\" "
                           + "text=\"" + _config.getFgColor() + "\" "
                           + "link=\"" + _config.getNickColor() + "\">"
                           + _conversation.toString()
                           + "</body></html>");
        
        _convoPane.setBackground( Color.decode( _config.getBgColor() ) );
    }
    
    public void displayMessage( String nick, String text ) {
        MessageRunnable mr = new MessageRunnable( this, nick, text );
        Log.debug("Creating thread to display: " + text);
        
        if( _clientWindow.isVisible() ) {
            SwingUtilities.invokeLater( mr );
        }
    }
}

class MessageRunnable implements Runnable {
    ClientPanel _cp = null;
    String _message = "";
    String _nick = "";
    
    public MessageRunnable( ClientPanel cp, String nick, String message ) {
        super();
        _cp = cp;
        _nick = nick;
        _message = message;
    }
    
    public void run() {
        if( _cp != null ) {
            formatMessage();
            makeUrlsClickable();
            _cp.setConvoText( _nick, _message );
            _cp.adjustScrollPane();
            _cp.adjustTitle();
        }
    }
    
    public void formatMessage() {
        _message = _message.replaceAll( "<", "&lt;");
        _message = _message.replaceAll( ">", "&gt;");
        _message = _message.replaceAll( "  ", " &nbsp;");
    }
    
    public void makeUrlsClickable() {
        StringBuffer _newMessage = new StringBuffer();
        String strRegex = "((([A-Za-z]+://){0,1}"
            + "(([0-9a-z_!~*'().&=+$%-]+: )?[0-9A-Za-z_!~*'().&=+$%-]+@)?" //user@
            + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP- 199.194.52.184
            + "|" // allows either IP or domain
            + "([0-9A-Za-z_!~*'()-]+\\.)*" // tertiary domain(s)- www.
            + "([0-9A-Za-z][0-9A-Za-z-]{0,255})?[0-9A-Za-z]\\." // second level domain            
            + "[A-Za-z]{2,6})" // first level domain- .com or .museum
            + "(:[0-9]{1,5})?" // port number- :80
            + "((/?)|" // a slash isn't required if there is no file name
            + "(/[0-9A-Za-z_*!~'().;?:@&=+$,%#-]+)+)/?)"
            + "([ !?.]+|$))"; // Stuff to mark a url is finished
        
        Pattern urlPattern = Pattern.compile( strRegex );
        
        // Replace all occurrences of pattern in input
        Matcher urlMatcher = urlPattern.matcher( _message );
        
        // Find all the matches.
        int previousEnd = 0;
        while( urlMatcher.find() ) {
            String url = urlMatcher.group();
            int groupCount = urlMatcher.groupCount();
            for( int i=0; i<groupCount; i++ ) {
                String groupStr = urlMatcher.group(i);
            }
            String clickableURL = url;
            
            // HyperlinkListener won't pass url if protocol isn't in front
            // Assuming http 
            if( ! url.matches( "^[A-Za-z]+://.*" ) ) {
                clickableURL = "http://" + url;
            }
            
            _newMessage.append( _message.substring( previousEnd, urlMatcher.start()));
            _newMessage.append( "<a href=\"" + clickableURL + "\">" + url + "</a>" );
            previousEnd = urlMatcher.end();
        }
        _newMessage.append( _message.substring( previousEnd, _message.length() ) );
        
        _message = _newMessage.toString();
    }
}

class LinkHandler implements HyperlinkListener {
    public void hyperlinkUpdate( HyperlinkEvent e ) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            BrowserControl.displayURL( e.getURL().toString() );
        }
    }
}
