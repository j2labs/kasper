package com.kasper.chat.config;

import java.util.Calendar;

public class ConversationProps {
    private String _lastMessageFrom = "";
    private Calendar _lastReceivedMessageAt = null;
    private Calendar _lastSentMessageAt = null;

    public ConversationProps() {}

    public void updateLastReceivedAt() {
        _lastReceivedMessageAt = Calendar.getInstance();
    }

    public Calendar getLastReceivedAt() {
        return _lastReceivedMessageAt;
    }

    public String formattedLastReceivedAt() {
        return formattedDate( _lastReceivedMessageAt );
    }

    public void updateLastSentAt() {
        _lastSentMessageAt = Calendar.getInstance();
    }

    public Calendar getLastSentAt() {
        return _lastSentMessageAt;
    }

    public String formattedLastSentAt() {
        return formattedDate( _lastSentMessageAt );
    }

    private String formattedDate( Calendar cal ) {
        StringBuffer formattedDate = new StringBuffer();

        formattedDate.append( cal.get( Calendar.HOUR ) + ":" );
        formattedDate.append( cal.get( Calendar.MINUTE ) + " " );
        formattedDate.append( cal.get( Calendar.AM_PM ) );

        return formattedDate.toString();
    }

    public void setLastMessageFrom( String lmf ) {
        _lastMessageFrom = lmf;
    }

    public String getLastMessageFrom() {
        return _lastMessageFrom;
    }
}
