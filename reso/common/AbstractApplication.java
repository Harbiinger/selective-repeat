/*******************************************************************************
 * Copyright (c) 2011 Bruno Quoitin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Bruno Quoitin - initial API and implementation
 ******************************************************************************/
package reso.common;

/**
 * This class models a process running on a host.
 */
public abstract class AbstractApplication
{

	/** The host for this application. */
    protected final Host host;
    /** The name of this application. */
    public final String name;

    /**
     * Creates a new application.
     * 
     * @param host is the host where the application is running.
     * @param name is the name of the application.
     */
    public AbstractApplication(Host host, String name) {
    	this.host= host;
    	this.name= name;
    }

    /**
 	 * Get the host of this application.
 	 * 
     * @return the host of this application.
     */
    public Host getHost() {
    	return host;
    }

    /**
     * Start this application.
     * 
     * Note : when a host is started, all its application are started.
     * 
     * @throws Exception
     */
    public abstract void start()
    	throws Exception;
    
    /**
     * Stop this application.
     * 
     * Note : when a host is stopped, all its application are stopped.
     */
    public abstract void stop();

}
