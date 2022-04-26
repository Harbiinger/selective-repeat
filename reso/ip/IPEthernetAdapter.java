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

import reso.common.HardwareInterface;
import reso.common.Interface;
import reso.common.InterfaceAttrListener;
import reso.common.MessageListener;
import reso.ethernet.EthernetAddress;
import reso.ethernet.EthernetFrame;
import reso.ethernet.EthernetInterface;
import reso.ip.arp.ARPProtocol;

public class IPEthernetAdapter
extends AbstractIPInterfaceAdapter
implements MessageListener<EthernetFrame>, InterfaceAttrListener {

	public final IPLayer ip;
	public final EthernetInterface iface;
    private ARPProtocol arp;
	
	public IPEthernetAdapter(IPLayer ip, EthernetInterface iface) {
		super(ip, iface.getType());
		setIndex(iface.getIndex());
		this.ip= ip;
		this.iface= iface;
		arp= new ARPProtocol(this);
		iface.addListener(this);
		iface.addAttrListener(this);
	}
	
	@Override
	public void receive(Datagram m) throws Exception {
		ip.receive(this, m);
	}

	@Override
	public void send(Datagram datagram, IPAddress gateway) throws Exception {
    	if (!iface.isConnected()) {
    		System.err.println("Interface is not connected [" + toString() + "]");
    		return;
    	}
    	
    	IPAddress ipAddr;
    	if (gateway != null)
    		ipAddr= gateway;
    	else
    		ipAddr= datagram.dst;
    	
    	EthernetAddress maddr;
    	if (ipAddr.isBroadcast())
    		maddr= EthernetAddress.BROADCAST;
    	else
    		maddr= arp.getMapping(ipAddr);
    	
    	if (maddr != null) {
    		EthernetFrame frame= new EthernetFrame(iface.addr, maddr, EthernetFrame.PROTO.IP, datagram);
    		iface.send(frame);
    	} else {
    		arp.performARPRequest(ipAddr, datagram);
    	}
	}

	@Override
	public void receive(HardwareInterface<EthernetFrame> iface, EthernetFrame msg) throws Exception {
    	if (msg.dst.isBroadcast() || this.iface.addr.equals(msg.dst)) {
    		switch (msg.protocol) {
    		case ARP:
    			arp.handleARPMessage(msg);
    			break;
    		case IP:
    			ip.receive(this, (Datagram) msg.payload);
    			break;
    		}

    	}
	}

	@Override
	public void attrChanged(Interface iface, String attr) {
		if (attr.equals(STATE))
			setAttribute(STATE, iface.getAttribute(attr));
	}
	
	/* Add a static ARP entry */
	public void addARPEntry(IPAddress addr, EthernetAddress maddr) {
		arp.addMapping(addr, maddr);
	}
	
}
