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
import reso.ip.Datagram;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
import reso.ip.IPLayer;
import reso.utilities.MonitorIdMetric;

/**
 * This class is an application that receives probe packets and
 * collects some statistics such as the packet end-to-end delay and packet count.
 * 
 * @see ProbeMessage
 * @see AppPacketSource
 */
public class AppPacketSink
    extends AbstractApplication
{ 
	
	private final IPLayer ip;
	private final MonitorIdMetric<Double> monDelay; /** Monitor that collects received packets' delay */
	private int pktCount= 0;                        /** Counts received packets */ 
       
	/**
	 * Creates a new @class AppPacketSink.
	 * 
	 * @param host is the IP host on which this application will be running.
	 * @param monDelay is a monitor used to collect the delay of all the received packets.
	 */
    public AppPacketSink(IPHost host, MonitorIdMetric<Double> monDelay) {	
    	super(host, "sink");
    	ip= host.getIPLayer();
    	this.monDelay= monDelay;
    }

    /**
     * Creates a new @class AppPacketSink.
	 * 
	 * @param host is the IP host on which this application will be running.
     */
    public AppPacketSink(IPHost host) {
    	this(host, null);
    }

    public void start()
    throws Exception {
    	ip.addListener(AppPacketSource.IP_PROTO_PROBE, new InternalIPInterfaceListener());
    }
    
    public void stop() {}

	/**
	 * Returns the number of received packets.
	 * 
	 * @return number of received packets.
	 */
	public int getPacketCount() {
		return pktCount;
	}
    
	private class InternalIPInterfaceListener
	implements IPInterfaceListener {
		
		@Override
		public void receive(IPInterfaceAdapter src, Datagram datagram)
				throws Exception {
			pktCount++;
			ProbeMessage msg= (ProbeMessage) datagram.getPayload();
			double arrivalTime= host.getNetwork().getScheduler().getCurrentTime();
			double e2eDelay= arrivalTime - msg.departureTime;
			if (monDelay == null)
				return;
			monDelay.record(msg.seqNum, e2eDelay);
			//System.out.println("arrival rate = " + ((1.0 * pktCount) / arrivalTime) + " pkt/s");
		}
		
	}
	
}

