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
package reso.ip.arp;

import java.util.HashMap;
import java.util.Map;

import reso.common.AbstractTimer;
import reso.common.Task;
import reso.ethernet.EthernetAddress;
import reso.ethernet.EthernetFrame;
import reso.ethernet.EthernetInterface;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPEthernetAdapter;
import reso.scheduler.AbstractEvent;
import reso.scheduler.AbstractScheduler;

public class ARPProtocol {
	
	/** The wake-up period for the ARP entries aging timer. */
	public static final double AGING_TIMER_INTERVAL= 1.0;
	
	/** The ARP table. */
    private Map<IPAddress, ARPEntry> tableARP=
    	new HashMap<IPAddress, ARPEntry>();
    
    /** The list of pending tasks (datagrams that need to be sent when
     *  the destination MAC address will be known). */
    private Map<IPAddress, Task> pendingARPTasks=
    	new HashMap<IPAddress, Task>();
    
    /** The Ethernet interface for this instance of ARP */
    private final EthernetInterface iface;
    /** The IP adapter for the Ethernet interface. */
    private final IPEthernetAdapter adapter;
    /** The aging timer. */
    private AbstractTimer agingTimer;
    
    /**
     * Creates an instance of ARP for an IP Ethernet adapter.
     * 
     * @param adapter is the IP adapter for an Ethernet interface.
     */
    public ARPProtocol(IPEthernetAdapter adapter) {
    	this.adapter= adapter;
    	this.iface= adapter.iface;
    }

    
    private class TaskSendFrame
    implements Task {
    	public final Datagram datagram;
    	public final IPAddress gateway;
    	public final EthernetInterface iface; 
    	public TaskSendFrame(Datagram datagram, IPAddress gateway, EthernetInterface iface) {
    		this.datagram= datagram;
    		this.gateway= gateway;
    		this.iface= iface;
    	}
    	public void run()
    	throws Exception {
        	EthernetAddress maddr= tableARP.get(gateway).getAddress();
        	if (maddr != null) {
        		EthernetFrame frame= new EthernetFrame(iface.addr, maddr, EthernetFrame.PROTO.IP, datagram);
        		iface.send(frame);
        	}
    	}
    }
    	
    /**
     * Get an ARP entry. An ARP entry will only be returned if it in state
     * REACHABLE or PERMANENT.
     *
     * @param addr is the IP address for which the corresponding Ethernet address is requested.
     */
    public EthernetAddress getMapping(IPAddress addr) {
    	ARPEntry entry= tableARP.get(addr);
    	if ((entry == null) || (entry.getState() == ARPEntry.STATE.INCOMPLETE))
    		return null;
    	return entry.addr;
    }
    
    /**
     * Insert a static ARP entry into the ARP table.
     * This entry will be marked as PERMANENT and never expire.
     * @param addr  is the IP address for this entry.
     * @param maddr is the Ethernet address for this entry.
     */
    public void addMapping(IPAddress addr, EthernetAddress maddr) {
    	ARPEntry entry= new ARPEntry(maddr);
    	tableARP.put(addr, entry);
    }
    
    /** Send an ARP request */
    public void performARPRequest(final IPAddress addr, Datagram datagram)
    throws Exception {
    	ARPEntry entry= tableARP.get(addr);
    	
    	/* An ARP Request is only sent if there is no entry */
    	if (entry == null) {
    		final ARPEntry newEntry= new ARPEntry();
    		tableARP.put(addr, newEntry);
    		EthernetFrame frame= new EthernetFrame(iface.addr, EthernetAddress.BROADCAST, EthernetFrame.PROTO.ARP,
        		ARPMessage.request(addr));
    		iface.send(frame);
    		/* Start timer for ARP response timeout */ 
    		iface.getNode().getNetwork().getScheduler().schedule(new AbstractEvent(iface.getNode().getNetwork().getScheduler().getCurrentTime() + 5) {
				public void run() throws Exception {
					if (newEntry.getState() == ARPEntry.STATE.INCOMPLETE)
						throw new Exception("ARP request timeout (" + addr + ")");
				}
			});
    		/* Start aging timer, if it was not running */
    		if (agingTimer == null)
    			agingTimer= new InternalAgingTimer(iface.getNode().getNetwork().getScheduler());
    		if (agingTimer.isRunning())
    			agingTimer.start();
    	}
    
    	/* The requesting datagram is queued. Only one datagram is queued per destination.
    	 * If another datagram was queued before, it will be replace by the newcomer. */
    	pendingARPTasks.put(addr, new TaskSendFrame(datagram, addr, iface));
    }
    
    /**
     * Handle an incoming ARP message (either a request or a response).
     * 
     * @param frame is the frame containing the ARP message.
     * 
     * @throws Exception
     */
    public void handleARPMessage(EthernetFrame frame)
    throws Exception {
    	//System.out.println(adapter.ip.host + " :: " + adapter+" handleARPMessage(" + frame + ")");
    	ARPMessage msg= (ARPMessage) frame.payload;
    	
    	switch (msg.type) {
    	case REQUEST:		
    		if (msg.ipAddr.isBroadcast() || !adapter.hasAddress(msg.ipAddr))
    			return;
    		ARPMessage arpMsg= ARPMessage.response(msg.ipAddr, iface.addr); 
    		EthernetFrame response= new EthernetFrame(iface.addr, frame.src, EthernetFrame.PROTO.ARP, arpMsg);
    		iface.send(response);
    		break;
    		
    	case RESPONSE:
    		/* An ARP Response should only be received after a request
               -> we do not support gratuitous ARP */
    		Task tsk= pendingARPTasks.get(msg.ipAddr);
    		if (tsk == null)
    			return;
    		ARPEntry entry= tableARP.get(msg.ipAddr);
    		assert entry != null;
    		assert entry.getState() == ARPEntry.STATE.INCOMPLETE; 
    		entry.reachable(msg.ethAddr);
    		tsk.run();
    		break;    		
    	}
    }
    
    /**
     * This timer is responsible for cleaning too old ARP entries.
     * The timer is ran every AGING_TIMER_INTERVAL seconds.
     * 
     * I then traverses the whole ARP table looking for reachable entries.
     * Those entries have their lifetime decreased and if the lifetime is expired,
     * they are removed from the table.
     */
    private class InternalAgingTimer
    extends AbstractTimer {
    	
    	public InternalAgingTimer(AbstractScheduler scheduler) {
    		super(scheduler, AGING_TIMER_INTERVAL, true);
    	}
    	
		protected void run() throws Exception {
			int countLife= 0;
			for (IPAddress addr: tableARP.keySet()) {
				ARPEntry entry= tableARP.get(addr);
				if (entry.getState() != ARPEntry.STATE.REACHABLE)
					continue;
				entry.lifetime-= AGING_TIMER_INTERVAL;
				if (entry.lifetime < 0)
					tableARP.remove(addr);
				else
					countLife++;
			}
			if (countLife == 0)
				stop();
		}
    	
    }
	
}
