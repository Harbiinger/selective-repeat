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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import reso.common.AbstractInterface;

public abstract class AbstractIPInterfaceAdapter
extends AbstractInterface
implements IPInterfaceAdapter {

	private final IPLayer ip;
	
	private Set<IPAddress> addresses= new TreeSet<IPAddress>();
    private List<IPInterfaceListener> listeners= new ArrayList<IPInterfaceListener>();
	
	public AbstractIPInterfaceAdapter(IPLayer ip, String type) {
		super(ip.host, type);
		this.ip= ip;
		defineAttribute(ATTR_METRIC, Integer.MAX_VALUE, false);
	}
	
    public void setMetric(int metric)
    throws Exception {
    	if (metric < 0)
    		throw new Exception("negative cost not accepted for interface " + ip.host.name + 
    			"." + getName());
    	setAttribute(ATTR_METRIC, metric);
    }
    
    public int getMetric() {
    	return (Integer) getAttribute(ATTR_METRIC);
    }
	
	@Override
	public boolean hasAddress(IPAddress addr) {
		return (addresses.contains(addr));
	}
	
	@Override
	public void addAddress(IPAddress addr) {
		addresses.add(addr);
	}
	
	@Override
	public IPAddress getAddress() {
		return addresses.iterator().next();
	}
	
	public void receive(Datagram m) throws Exception {
		for (IPInterfaceListener l: listeners)
			l.receive(this, m);
	}
	
	public void addListener(IPInterfaceListener l) {
		listeners.add(l);
	}
		
	public void removeListener(IPInterfaceListener l) {
		listeners.remove(l);
	}
		
	public IPLayer getIPLayer() {
		return ip;
	}
	
}
