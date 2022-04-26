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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import reso.ethernet.EthernetAddress;
import reso.scheduler.AbstractScheduler;

/**
 * This class is used to model a network of hosts and routers.
 * This class also maintains the scheduler for events in this network.
 */
public class Network {

	/** Scheduler for this network. */
	public final AbstractScheduler scheduler;
	private static long nextEthernetID= 0;
	
	/** Set of nodes (hosts and routers) in this network.*/
	private Map<String,Node> nodes= new TreeMap<String,Node>();
	
	/**
	 * Create a new Network.
	 * 
	 * @param scheduler is the scheduler for events in this network.
	 */
	public Network(AbstractScheduler scheduler) {
		this.scheduler= scheduler;
	}
	
	/**
	 * Add a node to this network. The new node cannot have the same name
	 * as a node already existing in this network.
	 * 
	 * @param node the node to be added.
	 * 
	 * @throws Exception
	 */
	public void addNode(Node node)
	throws Exception {
		if (nodes.containsKey(node.name))
			throw new Exception("Node [" + node.name + "] already exists");
		nodes.put(node.name, node);
		node.setNetwork(this);
	}
	
	/**
	 * Get the set of nodes.
	 * 
	 * @return the set of nodes.
	 */
	public Collection<Node> getNodes() {
		return nodes.values();
	}
	
	/**
	 * Get a node based on its name.
	 * 
	 * @param name is the name of the node.
	 * 
	 * @return the searched node or {@code null} if that node is unknown.
	 */
	public Node getNodeByName(String name) {
		return nodes.get(name);
	}
	
	/**
	 * Get the network's scheduler.
	 * 
	 * @return the network's scheduler.
	 */
	public AbstractScheduler getScheduler() {
		return scheduler;
	}
	
	/**
	 * Generate a new Ethernet address for this network.
	 * 
	 * @return an Ethernet address that has not yet been allocated.
	 */
	public static EthernetAddress generateEthernetAddress() {
		nextEthernetID+= 1;
		long id= nextEthernetID;
		int [] bytes= new int [] {0, 0, 0, 0, 0, 0};
		for (int i= 0; i < 6; i++) {
			bytes[5-i]= (int) (id % 256);
			id= id/256;
		}
		EthernetAddress addr= null;
		try {
			addr= EthernetAddress.getByAddress(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
		} catch (Exception e) {
		}
		return addr;
	}

}
