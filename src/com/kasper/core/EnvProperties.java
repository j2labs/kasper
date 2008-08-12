package com.kasper.core;

import java.util.Properties;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.kasper.core.Log;

public class EnvProperties {

    private static Properties _props = null;
    public final static String PROPS_COMMENT = "Kasper properties file. Do NOT edit by hand!";
    public final static String DEFAULT_PROPS_FILE = "kasper.properties";

    public static boolean loadProperties( String filename ) {
        boolean success = false;
        
        try {
            if( filename == null ) {
                filename = DEFAULT_PROPS_FILE;
            }
        
            FileInputStream propIS = new FileInputStream( filename );
            _props = new Properties();
            _props.load( propIS );
            success = true;
        }
        catch(IOException ioe) {
            Log.error("Could load properties file!");
            Log.error(ioe);
        }
        
        return success;
    }

    public static boolean saveProperties( String filename ) {
        boolean success = false;
        
        try {
            if( filename == null ) {
                filename = DEFAULT_PROPS_FILE;
            }
            
            FileOutputStream propOS = new FileOutputStream( filename );
            _props.store( propOS, PROPS_COMMENT );
            success = true;
        }
        catch(IOException ioe) {
            Log.error("Could not save properties file!");
            Log.error(ioe);
        }
        
        return success;
    }

    public static synchronized void setProperty( String key, String value ) {
        if( _props != null )
        _props.setProperty( key, value );
    }

    public static synchronized String getProperty( String key ) {
        if( _props == null ) {
            return null;
        }
        return _props.getProperty( key );
    }

    public static synchronized Enumeration<?> names() {
        if( _props == null ) {
            return null;
        }
        return _props.propertyNames();
    }
}
