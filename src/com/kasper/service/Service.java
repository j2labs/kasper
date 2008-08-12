package com.kasper.service;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.kasper.core.EnvProperties;
import com.kasper.core.EnvPropertyKeys;
import com.kasper.core.Log;
import com.kasper.ms.Message;

public abstract class Service {

    public final static String ARG_PREFIX = "--";
    public final static String ARG_NOARG = "nullity lets you divide by 0!";
    private final static String ARG_HELP = ARG_PREFIX + "help";
    private final static String ARG_PROPS = ARG_PREFIX + "props";
    private final static String ARG_HOSTNAME = ARG_PREFIX + "host";
    private final static String ARG_PORT = ARG_PREFIX + "port";
    private final static String ARG_LOG_LEVEL = ARG_PREFIX + "log-level";
    
    private ServiceConfig _serviceConfig = null;

    abstract public String getServiceName();
    abstract public String getServiceVersion();
    abstract public void additionalHelpInfo( StringBuffer help );
    abstract public void additionalProperties();
    abstract public void additionalCmdLineArgs( Map<String,String> arguments );
    abstract public void initServiceConfig();
    abstract public void initServiceLogic();
    abstract public void connecting( Socket socket );
    abstract public void disconnecting( Socket socket );
    abstract public void processMessage( Socket socket, Message message );

    public void setServiceConfig( ServiceConfig sc ) {
        _serviceConfig = sc;
    }

    public String helpInfo(  ) {
        StringBuffer help = new StringBuffer();
        
        help.append( "\n" + getServiceDescription() + "\n");
        help.append( ARG_PROPS + " <filename> chooses alternative properties filename.\n" );
        help.append( ARG_LOG_LEVEL + " [" + Log.NO_OUTPUT + "|" + Log.CRITICAL + "|" + Log.ERROR + "|"
                     + Log.WARNING + "|" + Log.INFO + "|" + Log.DEBUG + "]\n\tlevel of log output.\n" );
        help.append( ARG_HOSTNAME + " <hostname> use alternative hostname.\n" );
        help.append( ARG_PORT + " <port> use alternative port.\n" );
        help.append( "\n" ); // bringing attention to the second newline

        additionalHelpInfo( help );

        return help.toString();
    }

    public final void initService( String args[] ) {
        Map<String,String> cmdLineArgs = argsToMap( args );

        if( cmdLineArgs.get( ARG_HELP ) != null ) {
            System.out.println( helpInfo() );
            systemExit();
        }

        initServiceConfig();

        String propsFileName = cmdLineArgs.get( ARG_PROPS );
        if( propsFileName == null ) propsFileName = EnvProperties.DEFAULT_PROPS_FILE;
        
        processPropertiesFile( propsFileName );
        processCmdLineArgs( cmdLineArgs );

        Log.setLogLevel( _serviceConfig.getLogLevel() );

        initServiceLogic();
    }

    public String getServiceDescription() {
        return getServiceName() + " v" + getServiceVersion();
    }

    private void processPropertiesFile( String propsFileName ) {
        boolean success = EnvProperties.loadProperties( propsFileName );
        if( !success ) {
            Log.error( "Properties file could not be parsed. Exiting system." );
            systemExit();
        }

        String logLevel = EnvProperties.getProperty( EnvPropertyKeys.LOG_LEVEL );
        if( logLevel == null ) { logLevel = Log.DEFAULT_LOG_LEVEL; }

        _serviceConfig.setServerHostname( EnvProperties.getProperty( EnvPropertyKeys.MS_RMI_HOST ) );
        _serviceConfig.setServerPort( new Integer(EnvProperties.getProperty( EnvPropertyKeys.MS_RMI_PORT )) );
        _serviceConfig.setLogLevel( logLevel );

        additionalProperties();
    }

    private void processCmdLineArgs( Map<String,String> cmdLineArgs ) {
        String logLevel = cmdLineArgs.get( ARG_LOG_LEVEL );
        String hostname = cmdLineArgs.get( ARG_HOSTNAME );
        String port = cmdLineArgs.get( ARG_PORT );
        
        if( logLevel != null ) _serviceConfig.setLogLevel( logLevel );
        if( hostname != null ) _serviceConfig.setServerHostname( hostname );
        if( port != null ) _serviceConfig.setServerPort( new Integer( port ) );

        additionalCmdLineArgs( cmdLineArgs );
    }
 
    private Map<String,String> argsToMap( String args[] ) {
        Map<String,String> argsMap = new HashMap<String,String>();
        
        int size = args.length;
        String currentKey = null;
        for(int i=0; i<size; i++) {
            if( args[i].startsWith( ARG_PREFIX ) ) {
                currentKey = args[i];
                argsMap.put( currentKey, ARG_NOARG );
            }
            else {
                if( currentKey == null ) {
                    Log.error("No cmd line key for argument");
                    systemExit();
                }
                else {
                    argsMap.put( currentKey, args[i] );
                }
            }
        }

        return argsMap;
    }

    public void systemExit() {
        System.exit(1);
    }
}
