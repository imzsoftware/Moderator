package com.traq.utility;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.Invocable;
import java.io.*;

/**
 * Created by Amit on 30/11/15.
 */
public class Test {
        /**
         * Display a file in the system browser.  If you want to display a
         * file, you must include the absolute path name.
         *
         * @param url the file's url (the url must start with either "http://"
        or
         * "file://").
         */
        public static void displayURL(String url)
        {
            boolean windows = isWindowsPlatform();
            String cmd = null;
            try
            {
                if (windows)
                {
                    // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
                    cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
                    try{
                        Process p = Runtime.getRuntime().exec(cmd);
                    }catch(IOException ioe){
                        System.out.println("Exception .............");
                    }
                }
                else
                {
                    // Under Unix, Netscape has to be running for the "-remote"
                    // command to work.  So, we try sending the command and
                    // check for an exit value.  If the exit command is 0,
                    // it worked, otherwise we need to start the browser.
                    // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
                    cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
                    Process p = Runtime.getRuntime().exec(cmd);
                    try
                    {
                        // wait for exit code -- if it's 0, command worked,
                        // otherwise we need to start the browser up.
                        int exitCode = p.waitFor();
                        if (exitCode != 0)
                        {
                            // Command failed, start up the browser
                            // cmd = 'netscape http://www.javaworld.com'
                            cmd = UNIX_PATH + " "  + url;
                            p = Runtime.getRuntime().exec(cmd);
                        }
                    }
                    catch(InterruptedException x)
                    {
                        System.err.println("Error bringing up browser, cmd='" +
                                cmd + "'");
                        System.err.println("Caught: " + x);
                    }
                }
            }
            catch(IOException x)
            {
                // couldn't exec browser
                System.err.println("Could not invoke browser, command=" + cmd);
                System.err.println("Caught: " + x);
            }
        }
        /**
         * Try to determine whether this application is running under Windows
         * or some other platform by examing the "os.name" property.
         *
         * @return true if this application is running under a Windows OS
         */
        public static boolean isWindowsPlatform()
        {
            String os = System.getProperty("os.name");
            if ( os != null && os.startsWith(WIN_ID))
                return true;
            else
                return false;
        }
        /**
         * Simple example.
         */
        public static void main(String[] args)
        {
            //displayURL("http://www.javaworld.com");
            
            try {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine javascriptEngine = manager.getEngineByExtension("js");

                // Get script from JS File
/*                FileInputStream fileInputStream = new FileInputStream("C:\\Distance_Between\\backend\\Distance\\Flight.js");
                if (fileInputStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));

                    javascriptEngine.eval(reader);
                    Invocable invocableEngine = (Invocable)javascriptEngine;

                    // Invoke javascript function named "sayHello" with parameter "Atul"
                    Object object = invocableEngine.invokeFunction("CalcDistanceBetween", new String[]{"10.12333","10.43222","21.21234","21.923452"});
                    System.out.println("Result: " + object);
                }*/
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        // Used to identify the windows platform.
        private static final String WIN_ID = "Windows";
        // The default system browser under windows.
        private static final String WIN_PATH = "C:\\Program Files\\Internet Explorer\\iexplore";
        // The flag to display a url.
        private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
        // The default browser under unix.
        private static final String UNIX_PATH = "netscape";
        // The flag to display a url.
        private static final String UNIX_FLAG = "-remote openURL";







}




