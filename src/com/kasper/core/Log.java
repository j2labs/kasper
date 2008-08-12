package com.kasper.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

public class Log {

    public static final String LOG_OPEN = "[ ";
    public static final String LOG_CLOSE = " ]";
    public static final String LOG_DELIM = " | ";

    private static int NUM_NO_OUTPUT = 6;
    private static int NUM_CRITICAL = 5;
    private static int NUM_ERROR = 4;
    private static int NUM_WARNING = 3;
    private static int NUM_INFO = 2;
    private static int NUM_DEBUG = 1;

    public static final String NO_OUTPUT = "NO_OUTPUT";
    public static final String CRITICAL = "CRITICAL";
    public static final String ERROR = "ERROR";
    public static final String WARNING = "WARNING";
    public static final String INFO = "INFO";
    public static final String DEBUG = "DEBUG";

    public static final String DEFAULT_LOG_LEVEL = WARNING;
    private static int NUM_DEFAULT_LOG_LEVEL = 3;

    private static String _chosenLogLevel = null;

    public static void setLogLevel( String logLevel ) {
        _chosenLogLevel = logLevel;
    }

    private static int mapToNum( String level ) {
        if( level == null ) return NUM_DEFAULT_LOG_LEVEL; // system probably hasn't initialized
        else if( level.equals( CRITICAL ) ) return NUM_CRITICAL;
        else if( level.equals( ERROR ) ) return NUM_ERROR;
        else if( level.equals( WARNING ) ) return NUM_WARNING;
        else if( level.equals( INFO ) ) return NUM_INFO;
        else if ( level.equals( DEBUG ) ) return NUM_DEBUG;
        else return NUM_NO_OUTPUT;
    }
    
    public static void critical( String text ) { printToLog( NUM_CRITICAL, text ); }
    public static void critical( Exception e ) { printToLog( NUM_CRITICAL, e ); }

    public static void error( String text ) { printToLog( NUM_ERROR, text ); }
    public static void error( Exception e ) { printToLog( NUM_ERROR, e ); }
    
    public static void warning( String text ) { printToLog( NUM_WARNING, text ); }
    public static void warning( Exception e ) { printToLog( NUM_WARNING, e ); }
    
    public static void info( String text ) { printToLog( NUM_INFO, text ); }
    public static void info( Exception e ) { printToLog( NUM_INFO, e ); }
    
    public static void debug( String text ) { printToLog( NUM_DEBUG, text ); }
    public static void debug( Exception e ) { printToLog( NUM_DEBUG, e ); }

    private static String throwableToString( Throwable t ) {
        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    private static void printToLog( int level, Exception e ) {
        String text = throwableToString( e );
        printToLog( level, "\n" + text + "\n" );
    }
    
    private static void printToLog( int level, String text ) {
        int userLevel = mapToNum( _chosenLogLevel );
        if( userLevel > level ) {
            return;
        }
                
        StringBuffer logOutput = new StringBuffer();

        String formattedDate =
            DateFormat.getDateTimeInstance( DateFormat.SHORT,
                                            DateFormat.SHORT ).format( new Date() );
        
        logOutput.append( LOG_OPEN );
        logOutput.append( formattedDate );
        logOutput.append( LOG_DELIM + level + LOG_DELIM );
        logOutput.append( text );
        logOutput.append( LOG_CLOSE );

        System.out.println( logOutput.toString() );
    }
}
