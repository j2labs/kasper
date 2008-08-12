package com.kasper.chat.clients;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.kasper.core.Log;

public class ClientWindow extends JFrame {
    static final long serialVersionUID = -650145461178355799L;
    
    private boolean _focus = false;

    private JTabbedPane _tabbedPane = null;
    private ChatClient _chatClient = null;

    public static String activityMarker = "(*) ";

	public ClientWindow(ChatClient chatClient, String username) {
        _chatClient = chatClient;
        
        _tabbedPane = new JTabbedPane( _chatClient.getChatConfig().getTabPlacement() );

        setTitle( _chatClient.getServiceName() );
		setSize( 550, 450 );

        getContentPane().add( _tabbedPane, BorderLayout.CENTER );
        
		addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					dispose();
                    _chatClient.systemExit();
				}
			});

        addWindowFocusListener( new WindowAdapter() {
                public void windowGainedFocus(WindowEvent e) {
                    _focus = true;
                    adjustTitle();
                    ClientPanel cp = getCurrentClientPanel();
                    if( cp != null ) {
                        cp.gainedFocus();
                    }
                }
                public void windowLostFocus(WindowEvent e) {
                    _focus = false;
                }
            });

		setVisible( true );
	}

    public void setCurrentClientPanel( int idx ) {
        if( idx > -1 && idx < _tabbedPane.getTabCount() ) {
            _tabbedPane.setSelectedIndex( idx );
        }
    }

    public ClientPanel getCurrentClientPanel() {
        ClientPanel cp = (ClientPanel) _tabbedPane.getSelectedComponent();
        return cp;
    }

    public void displayMessageToAllPanels( String nick, String text ) {
        int numOfTabs = _tabbedPane.getTabCount();

        for( int i=0; i<numOfTabs; i++ ) {
            ClientPanel cp = (ClientPanel) _tabbedPane.getComponentAt( i );
            cp.displayMessage( nick, text );
        }
    }

    public void displayMessage( String label, String nick, String text ) {
        if( label.equals( ChatClient.ALL_CHANNELS ) ) {
            displayMessageToAllPanels( nick, text );
        }
        else {   
            ClientPanel cp = findClientPanel( label );
            if( cp == null ) {
                cp = getCurrentClientPanel();
            }

            // if cp *still* is null, no panels exist
            // This happens during login. can be more elegantly done,
            // but ignoring for now let's auto_join property build a panel
            if( cp == null ) {
                return;
            }

            cp.displayMessage( nick, text);
        }
    }

    public boolean createChannelTab( String channel ) {
        boolean returnVal = false;
        if( findClientPanel( channel ) == null ) {
            ClientPanel cp = new ClientPanel( this, channel );
            _tabbedPane.add( channel, cp );
            returnVal = true;
        }
        return returnVal;
    }

    public ClientPanel findClientPanel( String title ) {
        int numOfTabs = _tabbedPane.getTabCount();

        for( int i=0; i<numOfTabs; i++ ) {
            ClientPanel cp = (ClientPanel) _tabbedPane.getComponentAt( i );
            if( cp.getTitle().equals( title ) ) {
                return cp;
            }
        }

        return null;
    }

    public int findClientPanelIndex( ClientPanel cp ) {
        int numOfTabs = _tabbedPane.getTabCount();

        for( int i=0; i<numOfTabs; i++ ) {
            ClientPanel cpIteration = (ClientPanel) _tabbedPane.getComponentAt( i );
            if( cp.getTitle().equals( cpIteration.getTitle() )) {
                return i;
            }
        }

        return -1;
    }

    public boolean removeChannelTab( String channel ) {
        ClientPanel cp = findClientPanel( channel );

        if( cp != null ) {
            _tabbedPane.remove( cp );
            return true;
        }
        else {
            return false;
        }
    }

    public ChatClient getChatClient() { return _chatClient; }

    public void adjustTitle() {
        if(! _focus) {
            setTitle( activityMarker + _chatClient.getServiceName() );
        }
        else {
            setTitle( _chatClient.getServiceName() );
        }
    }

    public void adjustPanelTitle( ClientPanel cp, String title ) {
        int index = findClientPanelIndex( cp );
        Log.debug( "Request to adjust panel at index: " + index );
        if( index > -1 ) {
            Log.debug( "Adjusting title for " + cp.getTitle() + " to be: " + title );
            _tabbedPane.setTitleAt( index, title );
        }
    }

    public void alertUser( String alert ) {
        JOptionPane pane = new JOptionPane( alert );
        JDialog dialog = pane.createDialog( this, "Kasper alert!" );
        dialog.setModal( false );
        dialog.setVisible( true );
        dialog.setAlwaysOnTop( true );
    }

    public int askUser( String question ) {
        return JOptionPane.showConfirmDialog( null,
                                              question,
                                              "Kasper alert!",
                                              JOptionPane.OK_CANCEL_OPTION );
    }
}
