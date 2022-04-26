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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reso.common.Host;
import reso.common.Message;

/**
 * This class models the IP layer in a node. It is responsible
 * <ul>
 *   <li>for sending and receiving IP datagrams</li>
 *   <li>determining the outgoing interface and gateway for a destination IP address</li>
 *   <li>decreasing the TTL of IP datagrams when they are forwarded</li>
 *   <li>delivering locally destined IP datagrams to the upper layer</li>
 *   <li>maintaining a list of "logical interfaces" called adapters that
 *       allow to send IP datagrams through layer-2 interfaces (e.g. Ethernet)</li>
 *   <li>maintaining a list of routes</li>
 * </ul>
 * 
 * Note : the current model does not support longest prefix match but only exact match.
 */
public class IPLayer
	implements IPInterfaceListener
{

	/** The host running this IP layer instance. */
	public final Host host;
	/** The forwarding information base. */
    private final FIB fib= new FIB();
    /** Enable/disable forwarding.*/
    private boolean forwarding= false;
	
    /** The list of IP adapters. */
    private final List<IPInterfaceAdapter> ifaces=
    	new ArrayList<IPInterfaceAdapter>();
    /** The list of IP adapters, indexed by name. */
    private final Map<String, IPInterfaceAdapter> ifacesByName=
    	new HashMap<String, IPInterfaceAdapter>();
    
    private final List<IPInterfaceListener> rawListeners=
       	new ArrayList<IPInterfaceListener>();
    private final HashMap<Integer,List<IPInterfaceListener>> listeners=
      	new HashMap<Integer,List<IPInterfaceListener>>();
    
    /**
     * Create an IP layer for a host.
     * 
     * @param host is the host running this IP layer.
     */
    public IPLayer(Host host) {
    	this.host= host;
    }
    
    /**
     * Add an IP adapter.
     * 
     * @param iface is the new IP adapter.
     */
    public void addInterface(IPInterfaceAdapter iface) {
    	ifaces.add(iface);
    	ifacesByName.put(iface.getName(), iface);
    	iface.addListener(this);
    }
    
    /**
     * Get an IP adapter by name.
     * 
     * @param name is the name of the IP adapter.
     * @return
     */
    public IPInterfaceAdapter getInterfaceByName(String name) {
    	return ifacesByName.get(name);
    }
    
    /**
     * Get the set of IP adapters.
     * 
     * @return
     */
    public Collection<IPInterfaceAdapter> getInterfaces() {
    	return ifaces;
    }
    
    /**
     * Enable forwarding.
     */
    public void enableForwarding() {
    	forwarding= true;
    }
    
    /**
     * Disable forwarding.
     */
    public void disableForwarding() {
    	forwarding= false;
    }
    
    /**
     * Test if forwarding is enabled/disabled.
     * 
     * @return true if forwarding is enabled.
     */
    public boolean isForwardingEnabled() {
    	return forwarding;
    }
    
    /**
     * Test if the host owns an IP address, that if this IP layer
     * has an adapter with that IP address.
     * 
     * @param addr is the IP address to be tested.
     * @return true if the host onws that IP address.
     */
    public boolean hasAddress(IPAddress addr) {
    	for (IPInterfaceAdapter iface: ifaces)
    		if (iface.hasAddress(addr))
    			return true;
    	return false;
    }
    
    /**
     * Handle an IP datagram received from an IP adapter.
     * 
     * <p>The received IP datagram is delivered locally if the destination address
     * is either broadcast or owned by the local host.
     * </p>
     * 
     * <p>Otherwise, if forwarding is enabled, a lookup is performed in the FIB
     * and the datagram is forwarded through the outgoing interface, possibly
     * through a gateway.
     * </p>
     * 
     * @param iface is the receiving IP adapter.
     * @param msg is the received IP datagram.
     */
	public void receive(IPInterfaceAdapter iface, Datagram msg)
	throws Exception {
		// Raw listeners are called for any (IP) protocol, even if
		// the datagram's destination is not this node
		for (IPInterfaceListener l: rawListeners)
			l.receive(iface, msg);
		
		// Datagrams addressed to this node
		if (hasAddress(msg.dst) || msg.dst.isBroadcast()) {
			List<IPInterfaceListener> listeners=
				this.listeners.get(msg.getProtocol());
			if (listeners != null)
				for (IPInterfaceListener l: listeners)
					l.receive(iface, msg);
			return;
		}
		
		// Datagram not addressed to this node. Forward if enabled...
		if (forwarding)
			forward(msg);
	}
	
	/**
	 * Forward an IP datagram.
	 * 
	 * @param msg is the IP datagram to be forwarded.
	 * @throws Exception
	 */
	protected void forward(Datagram msg)
	throws Exception {
		if (msg.dst.isBroadcast())
			return;
		if (msg.getTTL() <= 1)
			return;
		msg.decTTL();
    	IPRouteEntry re= fib.lookup(msg.dst);
    	if (re == null)
    		throw new Exception("Destination unreachable [" + msg.dst + "]");
    	if (re.oif != null) {
    		re.oif.send(msg, re.gateway);
    	} else {
    		throw new Exception("Routing through gateway is not supported");
    	}
	}
	
	/**
	 * Add a listener for any (IP) protocol.
	 * This listener will be called even for datagrams that are
	 * not destined to this node (kind or promiscuous mode).
	 */
    public void addRawListener(IPInterfaceListener l) {
    	rawListeners.add(l);
    }
    
    /**
     * Add a listener for a specific protocol number.
     * This listener will only be called when a received datagram
     * is destined to the local node and when the datagram's protocol number
     * matches that of the listener. */
    public void addListener(int protocol, IPInterfaceListener l) {
    	List<IPInterfaceListener> listeners= this.listeners.get(protocol);
    	if (listeners == null) {
    		listeners= new ArrayList<IPInterfaceListener>();
    		this.listeners.put(protocol, listeners);
    	}
    	listeners.add(l);
    }
    
    /**
     * Remove a listener for a specific protocol.
     */
    public void removeListener(int protocol, IPInterfaceListener l) {
    	List<IPInterfaceListener> listeners= this.listeners.get(protocol);
    	if (listeners == null)
    		return;
    	listeners.remove(listeners);
    }
    
    /**
     * Add a route to an IP address through an outgoing IP adapter.
     * The IP adapter is identified by its name.
     * 
     * @param dst  is the route's destination (IP address).
     * @param name is the name of the outgoing IP adapter.
     * 
     * @throws Exception
     */
    public void addRoute(IPAddress dst, String name)
		throws Exception {
    	IPInterfaceAdapter oif= ifacesByName.get(name);
    	if (oif == null)
    		throw new Exception("Unknown interface [" + name + "]");
    	IPRouteEntry re= new IPRouteEntry(dst, oif, "STATIC");
    	fib.add(re);
    }
    
    /**
     * Add a route to an IP address through an outgoing IP adapter.
     * The reference to the IP adapter is provided. This method
     * should only be used internally as the IP adapter must be owned
     * by the local host. 
     * 
     * @param dst is the route's destination (IP address). 
     * @param oif is the outgoing IP adapter.
     * 
     * @throws Exception
     */
    protected void addRoute(IPAddress dst, IPInterfaceAdapter oif)
	throws Exception {
    	if (!ifaces.contains(oif))
    		throw new Exception("Unknown interface [" + oif + "]");
    	IPRouteEntry re= new IPRouteEntry(dst, oif, "STATIC");
    	fib.add(re);
    }

    /**
     * Add a route to an IP address through a gateway.
     * 
     * @param dst     is the routes's destination (IP address).
     * @param gateway is the gateway's IP address.
     * 
     * @throws Exception
     */
    public void addRoute(IPAddress dst, IPAddress gateway)
    	throws Exception {
    	IPRouteEntry re= new IPRouteEntry(dst, gateway, "STATIC");
    	fib.add(re);
    }
    
    /**
     * Add a route entry.
     * This method can be used by routing protocols to populate the FIB.
     * 
     * @param re is the new route entry.
     * 
     * @throws Exception
     */
    public void addRoute(IPRouteEntry re)
    throws Exception {
    	if ((re.oif != null) && !ifaces.contains(re.oif))
    		throw new Exception("Unknown interface [" + re.oif + "]");
    	fib.add(re);
    }
    
    /**
     * Remove a route to an IP address.
     * 
     * @param dst is the route's destination (IP address).
     */
    public void removeRoute(IPAddress dst) {
    	fib.remove(dst);
    }

    /**
     * Get the set of routes.
     * 
     * @return
     */
    public Collection<IPRouteEntry> getRoutes() {
    	return fib.getEntries();
    }
    
    /**
     * Get the route entry for an IP address.
     * 
     * @param dst is the route's destination (IP address).
     * @return
     */
    public IPRouteEntry getRouteTo(IPAddress dst) {
    	try {
    		return fib.lookup(dst);
    	} catch (Exception e) {
			return null;
		}
    }
    
    /**
     * Send an IP message.
     * This method can be used by upper layers (applications) to send IP datagrams.
     * 
     * @param src      is the IP datagram's source IP address.
     * @param dst      is the IP datagram's destination IP address.
     * @param protocol is the IP datagram's protocol number
     * @param payload  is the IP datagram's payload.
     * 
     * @throws Exception
     */
    public void send(IPAddress src, IPAddress dst, int protocol, Message payload)
		throws Exception {
    	//System.out.println("IPLayer::send(" + src + "," + dst + "," + protocol + "," + payload + ")");

    	IPRouteEntry re= fib.lookup(dst);
    	if (re == null)
    		throw new Exception("Destination unreachable [" + dst + "]");
    	
    	//System.out.println(" lookup -> " + re.oif + "," + re.gateway);
    	
    	Datagram datagram;
    	if (!src.isUndefined()) {
    		if (!hasAddress(src))
    			throw new Exception("IP address spoofing [" + src + "]");
    		datagram= new Datagram(src, dst, protocol, 255, payload);
    	} else
    		datagram= new Datagram(re.oif.getAddress(), dst, protocol, 255, payload);
    	re.oif.send(datagram, re.gateway);
	}

}
