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

import java.util.ArrayList;
import java.util.List;

public class Host
    extends Node
{

    private List<MessageListener<?>> listeners;
    private List<AbstractApplication> apps;

    public Host(String name) {
    	super(name);
    	this.apps= new ArrayList<AbstractApplication>();
    	this.listeners= new ArrayList<MessageListener<?>> ();
    }

    
    public void addApplication(AbstractApplication app)
	throws Exception {
    	if (app.getHost() != this)
    		throw new Exception("Application already belongs to another host");
    	apps.add(app);
    }

    public void addListener(MessageListener<?> l) {
    	listeners.add(l);
    }

    public void start() 
    throws Exception {
    	for (AbstractApplication app: apps)
    		app.start();
    }
    
    public void stop() {
    	for (AbstractApplication app: apps)
    		app.stop();    	
    }
    
}
