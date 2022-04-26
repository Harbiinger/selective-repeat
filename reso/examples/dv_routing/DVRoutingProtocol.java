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
package reso.examples.dv_routing;

import java.util.HashMap;
import java.util.Map;

import reso.common.AbstractApplication;
import reso.common.Interface;
import reso.common.InterfaceAttrListener;
import reso.examples.dv_routing.DVMessage.DV;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
import reso.ip.IPLayer;
import reso.ip.IPLoopbackAdapter;
import reso.ip.IPRouter;

public class DVRoutingProtocol
extends AbstractApplication
implements IPInterfaceListener, InterfaceAttrListener {

	public static final String PROTOCOL_NAME= "DV_ROUTING";
	public static final int IP_PROTO_DV= Datagram.allocateProtocolNumber(PROTOCOL_NAME);
	
	private final IPLayer ip;
	private final boolean advertise;
	
	private final Map<IPAddress,Map<IPAddress,DVMessage.DV>> table=
		new HashMap<IPAddress,Map<IPAddress,DVMessage.DV>>();
	
	/** Constructor
	 * 
	 * @param router is the router that hosts the routing protocol
	 * @param advertise specifies if this router will send DV for its own local destinations
	 */
	public DVRoutingProtocol(IPRouter router, boolean advertise) {
		super(router, PROTOCOL_NAME);
		this.ip= router.getIPLayer();
		this.advertise= advertise;
	}

	private IPAddress getRouterID() {
		IPAddress routerID= null;
		for (IPInterfaceAdapter iface: ip.getInterfaces()) {
			IPAddress addr= iface.getAddress();
			if (routerID == null)
				routerID= addr;
			else if (routerID.compareTo(addr) < 0)
				routerID= addr;
		}
		return routerID;
	}
	
	@Override
	public void start() throws Exception {
		// Register listener for datagrams with DV routing messages
		ip.addListener(IP_PROTO_DV, this);
		
		// Register interface attribute listeners to detect metric and status changes
		for (IPInterfaceAdapter iface: ip.getInterfaces())
			iface.addAttrListener(this);
		
		// Send initial DV
		if (advertise) {
			for (IPInterfaceAdapter iface: ip.getInterfaces()) {
				if (iface instanceof IPLoopbackAdapter)
					continue;
				DVMessage dvm= new DVMessage();
				//dvm.addDV(ip.getInterfaceByName("lo0").getAddress(), 0, null);
				dvm.addDV(getRouterID(), 0, null);
				iface.send(new Datagram(iface.getAddress(), IPAddress.BROADCAST, IP_PROTO_DV, 1, dvm), null);
			}
		}
	}
	
	public void stop() {
		ip.removeListener(IP_PROTO_DV, this);
		for (IPInterfaceAdapter iface: ip.getInterfaces())
			iface.removeAttrListener(this);
	}
	
	private void sendToAll(DVMessage.DV dv)
	throws Exception {
		for (IPInterfaceAdapter iface: ip.getInterfaces()) {
			if (iface instanceof IPLoopbackAdapter)
				continue;
			DVMessage dvm= new DVMessage();
			dvm.addDV(dv.dst, dv.metric, null);
			iface.send(new Datagram(iface.getAddress(), IPAddress.BROADCAST, IP_PROTO_DV, 1, dvm), null);
		}
	}

	private DVMessage.DV compute(Map<IPAddress,DVMessage.DV> dvs) {
		System.out.println("\tcompute");
		DVMessage.DV bestDV= null;
		for (IPAddress neighbor: dvs.keySet()) {
			DVMessage.DV dv= dvs.get(neighbor);
			if (neighbor.equals(IPAddress.ANY)) {
				System.out.println("\t\tcurrent best = " + dv);
				continue;
			}
			System.out.println("\t\t" + dv);
			if (dv.metric == Integer.MAX_VALUE)
				continue;
			if (bestDV == null)
				bestDV= dv;
			else if (bestDV.metric > dv.metric)
				bestDV= dv;
		}
		System.out.println("\t\tbest = " + bestDV);
		return bestDV;
	}
	
	public int addMetric(int m1, int m2) {
		if (((long) m1) + ((long) m2) > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return m1 + m2;
	}
	
	@Override
	public void receive(IPInterfaceAdapter iface, Datagram msg)
	throws Exception {
		
		System.out.println(((int) (host.getNetwork().getScheduler().getCurrentTime() * 1000)) + "ms " +
			host.name + " " + iface + " " + msg);
		
		DVMessage dvm= (DVMessage) msg.getPayload();
			
		for (DVMessage.DV dv: dvm.dvs) {

			// Filter local addresses
			if (ip.hasAddress(dv.dst))
				continue;
			
			// Get current DVs for this destination
			Map<IPAddress,DV> currentDVs= table.get(dv.dst);
			if (currentDVs == null) {
				
				// Add to RIB ...
				currentDVs= new HashMap<IPAddress,DV>();
				DVMessage.DV newDV= new DVMessage.DV(dv.dst, addMetric(dv.metric, iface.getMetric()), iface); 
				currentDVs.put(msg.src, newDV); // DV received from neighbor
				currentDVs.put(IPAddress.ANY, newDV); // Best DV
				table.put(dv.dst, currentDVs);
					
				// ... update FIB ...
				ip.addRoute(new DVRoutingEntry(newDV.dst, newDV.oif, newDV));
				
				// ... and propagate
				sendToAll(newDV);

			} else {
					
				DVMessage.DV newDV= new DV(dv.dst, addMetric(dv.metric, iface.getMetric()), iface);
				currentDVs.put(msg.src, newDV);
				
				// If best route has changed ...
				DVMessage.DV bestDV= compute(currentDVs);
				
				if (bestDV == null) {
					ip.removeRoute(dv.dst);
					continue;
				}
				
				if (currentDVs.get(IPAddress.ANY) == bestDV)
					continue;
				
				System.out.println("\tbest changed -> update !");
				
				// ... update RIB ...
				currentDVs.put(IPAddress.ANY, bestDV);
				
				// ... update FIB ...
				ip.addRoute(new DVRoutingEntry(bestDV.dst, bestDV.oif, bestDV));
				
				// ... and propagate
				sendToAll(newDV);

			}
								
		}
			
	}

	@Override
	public void attrChanged(Interface iface, String attr) {
		System.out.println("attribute \"" + attr + "\" changed on interface \"" + iface + "\" : " +
				iface.getAttribute(attr));
	}
		
}
