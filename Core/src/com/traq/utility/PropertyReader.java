/*
 * PropertyReader.java
 *
 * Created on May 20, 2006, 8:28 PM
 *
 */
package com.traq.utility;

import com.traq.common.Cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class PropertyReader {

    private String fileName = "";
    private Cache objCache = Cache.getInstance();
    private Properties cachedSettings = null;
    //private LogHandler logHndlr = new LogHandler();

    /**
     * Creates a new instance of PropertyReader
     */
    public PropertyReader(String inFile) {
        File settingFile = new File(this.fileName);
        ////System.out.println("Checking File " + inFile + " if available in cache");
        //check if KEY exists or not :
        if (objCache.get(inFile) != null) {

            //logHndlr.createLog("File '" + inFile + "' is available in cache,determining last access...", "RequestResponseLogs", 1);
            ////System.out.println("File '" + inFile + "' is available in cache..");
            //check if settingFile is modified after last access :
            Date fileLastAccessed = (Date) objCache.get(inFile + "_LastAccessed");

            //Load values from cache if File does not exists or exists and is not modified

            if (!settingFile.isFile() || (fileLastAccessed != null && fileLastAccessed.getTime() > settingFile.lastModified())) {
                //logHndlr.createLog("Getting value from cached property", "RequestResponseLogs", 1);
                this.cachedSettings = (Properties) objCache.get(inFile);
            } else {
                //logHndlr.createLog("File modified after last access, property file will be loaded again", "RequestResponseLogs", 1);
                this.cachedSettings = null;                         
            }
        } else {
            ////System.out.println("File '" + inFile + "' is not available in cache, defining cacheSettings as null");
            this.cachedSettings = null;
        }
        this.fileName = inFile;

    }

    private Properties getSettings() {

        Properties retVal = new Properties();
        //Now check if file exists or not :
        File settingFile = new File(this.fileName);
        if (settingFile.isFile()) {
            try {
                retVal.load(new FileInputStream(this.fileName));
                //bound to add loaded properties in Cache :
                this.cachedSettings = retVal;
                this.objCache.put(this.fileName, retVal);
                this.objCache.put(this.fileName + "_LastAccessed", new Date());
                //logHndlr.createLog("loading File " + this.fileName + " and its last access time in Cache...", "RequestResponseLogs", 1);
                return retVal;
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
                return null;
            }
        } else {
            return null;
        }


    }

    public String getProperty(String propertyName) {
        String retVal = "";
        Properties tmpProp = new Properties();
        try {
            if (this.cachedSettings == null) {
                tmpProp = this.getSettings();
            } else {
                tmpProp = this.cachedSettings;
            }
            retVal = tmpProp.getProperty(propertyName, "");
        } catch (NullPointerException nulEx) {
            //System.out.println(nulEx.getMessage());
        }
        return retVal;


    }
}
