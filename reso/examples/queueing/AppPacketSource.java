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
package reso.examples.queueing;

import reso.common.AbstractApplication;
import reso.common.AbstractTimer;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;
import reso.scheduler.AbstractScheduler;

public class AppPacketSource
    extends AbstractApplication
{ 
	
	private final IPLayer ip;
    private final IPAddress dst;
    private int pktLen= 0;
    
	protected final PacketGenerator generator;
    
    public static final int IP_PROTO_PROBE= Datagram.allocateProtocolNumber("PROBE");
    
    /**
     * Create a new @class AppPacketSource.
     * 
     * @param host is the IP host running this application.
     * @param dst is the packets' destination (could be running an instance of @class AppPacketSink).
     * @param pktLen is the packet length.
     * @param generator is the packet generator.
     */
    public AppPacketSource(IPHost host, IPAddress dst, int pktLen, PacketGenerator generator) {	
    	super(host, "source");
    	this.pktLen= pktLen;
    	this.dst= dst;
    	ip= host.getIPLayer();
    	this.generator= generator;
    }

    public void start()
    throws Exception {
    	sendNextPacket();
    }
    
    public void stop() {}
    
    /**
     * Send the next packet and schedules the next packet sending.
     * 
     * @throws Exception
     */
    protected void sendNextPacket() throws Exception {
    	double currentTime= host.getNetwork().getScheduler().getCurrentTime();
    	
    	System.out.println(String.format("%.6f", currentTime) +	"\tSource sends pkts");
    	
    	/* Send packets */
    	if (generator.hasMorePackets()) {
    		int seqNum= generator.nextPacket();
			ip.send(IPAddress.ANY, dst, IP_PROTO_PROBE, new ProbeMessage(pktLen, seqNum, currentTime));
    	}
		
		/* Schedule next burst */
    	if (generator.hasMorePackets()) {
    		InternalTimer timer= new InternalTimer(host.getNetwork().getScheduler(), generator.nextPacketInterval());
    		timer.start();
    	}
    	
    }

    private class InternalTimer extends AbstractTimer {
    	public InternalTimer(AbstractScheduler scheduler, double interval) {
    		super(scheduler, interval, false);
    	}
    	protected void run() throws Exception {
    		sendNextPacket();
		}
    }
    
}

