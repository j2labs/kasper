package com.kasper.util;

import java.io.IOException;

import com.kasper.core.Log;

public class BrowserControl {

    private static final String WIN_ID = "Windows";
    private static final String WIN_PATH = "rundll32";
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

    public static void displayURL(String url) {
        boolean windows = isWindowsPlatform();
        String cmd = null;
        
        try {
            if (windows) {
                cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
                Runtime.getRuntime().exec(cmd);
            }
            else {
                Log.warning("Only MS Windows is handled for now");
            }
        }
        catch(IOException ioe) {
            Log.error("Could not invoke browser, command=" + cmd);
            Log.error(ioe);
        }
    }

    public static boolean isWindowsPlatform() {
        String os = System.getProperty("os.name");
        if ( os != null && os.startsWith(WIN_ID))
            return true;
        else
            return false;
    }
}
