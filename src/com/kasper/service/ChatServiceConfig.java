package com.kasper.service;

public class ChatServiceConfig extends ServiceConfig {
	private String _chatServerHostname = null;
	private int _chatServerPort = 0;

	public void setChatServerHostname( String chatServerHostname ) { _chatServerHostname = chatServerHostname; }
	public String getChatServerHostname() { return _chatServerHostname; }

	public void setChatServerPort( int chatServerPort ) { _chatServerPort = chatServerPort; }
	public int getChatServerPort() { return _chatServerPort; }
}
