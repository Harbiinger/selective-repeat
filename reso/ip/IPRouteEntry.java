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

public class IPRouteEntry {

	public final IPAddress dst;
	public final IPAddress gateway;
	public final IPInterfaceAdapter oif;
	public final String type;
	
	public IPRouteEntry(IPAddress dst, IPAddress gateway, String type) {
		this.dst= dst;
    	this.gateway= gateway;
    	this.oif= null;
		this.type= type;
    }
	
    public IPRouteEntry(IPAddress dst, IPInterfaceAdapter oif, String type) {
		this.dst= dst;
    	this.gateway= null;
    	this.oif= oif;
		this.type= type;
    }

    public IPRouteEntry(IPAddress dst, IPInterfaceAdapter oif, IPAddress gateway, String type) {
    	this.dst= dst;
    	this.gateway= gateway;
    	this.oif= oif;
		this.type= type;
    }
     
    public String toString() {
    	String s= dst+" oif=" + oif;
    	if (gateway != null)
    		s+= ", gw="+gateway;
    	s+= ", type="+type;
    	return s;
    }

}
