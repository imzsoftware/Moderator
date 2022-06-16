package com.traq.utility;

import java.io.*;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.traq.common.Cache;

/**
 * Created by IntelliJ IDEA.
 * User: Amit
 * Date: 7 May, 2015
 * Time: 8:12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogHandler
{

    private Cache objCache;
    private Properties cachedProperties;

    public LogHandler(String confFile)
    {
        objCache = Cache.getInstance();
        cachedProperties = null;
        PropertyConfigurator.configure(confFile);
    }

    private void loadLog4JProperties(String settingFile)
    {
        File configFile = new File(settingFile);
        try
        {
            if(configFile.isFile() && configFile.canRead())
            {
                cachedProperties.load(new FileInputStream(settingFile));
                objCache.put("log4jConfig", cachedProperties);
                objCache.put("log4jConfig_LastAccessed", new Date());
            } else
            {
                cachedProperties = null;
            }
        }
        catch(IOException ioEx)
        {
            cachedProperties = null;
        }
    }

    public void createLog(String message, String loggerName, int i)
    {
        int j = 0;
        j = i;
        switch(j)
        {
        case 0: // '\0'
            Logger.getLogger(loggerName).debug(message);
            break;

        case 1: // '\001'
            Logger.getLogger(loggerName).info(message);
            break;

        case 2: // '\002'
            Logger.getLogger(loggerName).warn(message);
            break;

        case 3: // '\003'
            Logger.getLogger(loggerName).error(message);
            break;

        case 4: // '\004'
            Logger.getLogger(loggerName).fatal(message);
            break;

        default:
            Logger.getLogger(loggerName).debug(message);
            break;
        }
    }
}
