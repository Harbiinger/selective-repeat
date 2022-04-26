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
package reso.ip;

import reso.common.Host;

/** An IPHost is just a Host with an IPLayer. */
public class IPHost extends Host
{

    private final IPLayer ip;

    public IPHost(String name) {
    	super(name);
    	ip= new IPLayer(this);
    	try {
    		addApplication(new ICMPProtocol(this));
    	} catch (Exception e) { }
    }

    public IPLayer getIPLayer() {
    	return ip;
    }
    
}
