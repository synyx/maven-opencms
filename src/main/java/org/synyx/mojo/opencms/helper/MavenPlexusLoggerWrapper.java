/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.synyx.mojo.opencms.helper;

import org.apache.maven.plugin.logging.Log;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;


/**
 * This class wraps a Maven-Plugin-Logger to a Plexus-Logger
 * 
 * @author  macmac
 */
public class MavenPlexusLoggerWrapper extends AbstractLogger {

    /** reference to mavens logger */
    private Log mavenLogger;

    /**
     * Constructs a new instance. 
     * @param arg0 
     * @param arg1
     * @param mavenLogger Log Mavens Plugin Logger 
     */
    public MavenPlexusLoggerWrapper(int arg0, String arg1, Log mavenLogger) {
        super(arg0, arg1);
        this.mavenLogger = mavenLogger;
    }


    /**
     * 
     * @param arg0
     * @param arg1
     */
    public void debug(String arg0, Throwable arg1) {

        mavenLogger.debug(arg0, arg1);
    }


    /**
     * 
     * @param arg0
     * @param arg1
     */
    public void info(String arg0, Throwable arg1) {

        mavenLogger.info(arg0, arg1);
    }


    /**
     * 
     * @param arg0
     * @param arg1
     */
    public void warn(String arg0, Throwable arg1) {

        mavenLogger.warn(arg0, arg1);
    }


    /**
     * 
     * @param arg0
     * @param arg1
     */
    public void error(String arg0, Throwable arg1) {

        mavenLogger.error(arg0, arg1);
    }


    /**
     * 
     * @param arg0
     * @param arg1
     */
    public void fatalError(String arg0, Throwable arg1) {

        mavenLogger.error(arg0, arg1);
    }


    /**
     * 
     * @param arg0
     * @return
     */
    public Logger getChildLogger(String arg0) {

        return this;
    }
}
