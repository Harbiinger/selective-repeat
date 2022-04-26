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

import reso.common.Interface;

public interface IPInterfaceAdapter
extends Interface {

	String ATTR_METRIC= "metric";
	
	public String getName();
	public IPLayer getIPLayer();
	
	public boolean hasAddress(IPAddress addr);
	public void addAddress(IPAddress addr);
	public IPAddress getAddress();
	
    public void setMetric(int metric)
    	throws Exception;
    public int getMetric();
    
	public void send(Datagram datagram, IPAddress nexthop)
		throws Exception;
	
    public void addListener(IPInterfaceListener l);
    public void removeListener(IPInterfaceListener l);

}
