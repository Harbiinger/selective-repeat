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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** A Node is the simplest model of a networking equipment. A Node contains
 * physical interfaces. */
public abstract class Node {
	
    protected List<HardwareInterface<? extends Message>> ifaces;
    private Map<String, List<HardwareInterface<? extends Message>>> ifacesByType;
    private Map<String, HardwareInterface<? extends Message>> ifacesByName;
    public final String name;
    private Network network;
    
    public Node(String name) {
    	this.name= name;
    	this.ifaces= new ArrayList<HardwareInterface<? extends Message>>();
    	this.ifacesByType= new TreeMap<String, List<HardwareInterface<? extends Message>>>();
    	this.ifacesByName= new TreeMap<String, HardwareInterface<? extends Message>>();
    }
	
    /**
     * Add an interface to this node. The interface must have been
     * created with this host as "parent node".
     */
    public void addInterface(HardwareInterface<? extends Message> iface)
	throws Exception {
    	if (iface.getNode() != this)
    		throw new Exception("Interface already belongs to another host");
    	if (ifaces.contains(iface))
    		throw new Exception("Host already contains this interface [" + iface + "]");
		ifaces.add(iface);
				
		List<HardwareInterface<? extends Message>> ifacesList= ifacesByType.get(iface.getType());
		if (ifacesList == null) {
			ifacesList= new ArrayList<HardwareInterface<? extends Message>>();
			ifacesByType.put(iface.getType(), ifacesList);
		}
		iface.setIndex(ifacesList.size());
		ifacesList.add(iface);
		ifacesByName.put(iface.getName(), iface);
    }

    public HardwareInterface<? extends Message> getInterfaceByName(String name) {
    	return ifacesByName.get(name);
    }
    
    public Collection<HardwareInterface<? extends Message>> getInterfaces() {
    	return ifacesByName.values();
    }
    
    public void setNetwork(Network network) {
    	this.network= network;
    }
    
    public Network getNetwork() {
    	return network;
    }
            
    public String toString() {
    	return "Router [" + name + "]";
    }

}
