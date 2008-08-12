package com.kasper.service;

public class ServiceConfig {
	public static String DISCONNECT = "DISCONNECT";
	public static String CONNECT = "CONNECT";

	private String _serverHostname = null;
	private int _serverPort = 0;
    private String _logLevel = null;

	public void setServerHostname( String serverHostname ) { _serverHostname = serverHostname; }
	public String getServerHostname() { return _serverHostname; }

	public void setServerPort( int serverPort ) { _serverPort = serverPort; }
	public int getServerPort() { return _serverPort; }

    public void setLogLevel( String logLevel ) { _logLevel = logLevel; }
    public String getLogLevel() { return _logLevel; }
}
