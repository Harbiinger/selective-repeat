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
package reso.ethernet;

import java.util.LinkedList;
import java.util.Queue;

import reso.common.AbstractHardwareInterface;
import reso.common.Host;
import reso.scheduler.AbstractEvent;
import reso.scheduler.AbstractScheduler;
import reso.utilities.Monitor;
import reso.utilities.MonitorTimeMetric;

public class EthernetInterface
    extends AbstractHardwareInterface<EthernetFrame>
{

	// Queue of frames waiting to be sent.
	private Queue<EthernetFrame> queue= new LinkedList<EthernetFrame>();
	// Current state of interface
	private boolean sending= false;
	
	private MonitorTimeMetric<Integer> monQueue;
	private MonitorTimeMetric<Integer> monArrival;
	private MonitorTimeMetric<Integer> monDeparture;
	
	private int arrivalCount= 0;
	private int departureCount= 0;
	
	// Hardware address
    public final EthernetAddress addr;
    
    public EthernetInterface(Host host, EthernetAddress addr)
	throws Exception {
    	super(host, "eth");
    	this.addr= addr;
    }

    public boolean hasAddr(EthernetAddress addr) {
    	if (this.addr.equals(addr))
    		return true;
    	if (addr.isBroadcast())
    		return true;
    	return false;
    }
	    
    public void receive(EthernetFrame frame)
    throws Exception {
    	if (!isActive())
    		return;
    	if (frame.dst.isBroadcast() || addr.equals(frame.dst))
    		toListeners(frame);
    }

	public void send(EthernetFrame msg)
	throws Exception {
    	arrivalMonitor();
		// Drop frame if interface is not active
		if (!isActive())
			return;
		/* It is not possible to send two frames simultaneously on the same link.
		 * Therefore, the interface goes to state "sending" when it is currently
		 * sending a frame over the connected link. The interface stays in the
		 * "sending" state for the duration of the frame's transmission time.
		 * At the end of the transmission time, the message is effectively handed
		 * over to the link.
		 */
		if (!sending) {
			sending= true;
			_send(msg);
		} else {
			/* Messages received when the interface is in state "sending" are
			 * queued. until the current message has been sent. */
			queueMonitor("state", queue.size());
			queue.add(msg);
			queueMonitor("push", queue.size());
		}
    }
	
	private void _send(EthernetFrame frame)
	throws Exception {
		departureMonitor();
		toListeners(frame);
		AbstractScheduler sched= this.node.getNetwork().getScheduler();
		sched.schedule(new QueueEvent(sched.getCurrentTime() + getLink().getTransmissionDelay(frame.getByteLength()), frame));
	}
	
	private class QueueEvent extends AbstractEvent {
		
		private final EthernetFrame frame;

		public QueueEvent(double time, EthernetFrame frame) {
			super(time);
			this.frame=frame;
		}
		
		public void run() throws Exception {
			getLink().send(EthernetInterface.this, frame);
			if (!EthernetInterface.this.queue.isEmpty()) {
				queueMonitor("state", queue.size());
				EthernetFrame frame= EthernetInterface.this.queue.poll();
				queueMonitor("poll", queue.size());
				EthernetInterface.this._send(frame);
			} else
				EthernetInterface.this.sending= false;
		}
		
	}

	public Monitor getQueueMonitor(boolean display) {
		if (monQueue == null)
			monQueue= new MonitorTimeMetric<Integer>("queue", getNode().name + "." + getName(), display);
		return monQueue;
	}
	
	public Monitor getArrivalMonitor(boolean display) {
		if (monArrival == null)
			monArrival= new MonitorTimeMetric<Integer>("arrival", getNode().name + "." + getName(), display);
		return monArrival;
	}
	
	public Monitor getDepartureMonitor(boolean display) {
		if (monDeparture == null)
			monDeparture= new MonitorTimeMetric<Integer>("departure", getNode().name + "." + getName(), display);
		return monDeparture;
	}
	
	private void queueMonitor(String event, int size) {
		if (monQueue == null)
			return;
		monQueue.record(getNode().getNetwork().getScheduler().getCurrentTime(), size);
	}
	
	private void arrivalMonitor() {
		if (monArrival == null)
			return;
		monArrival.record(getNode().getNetwork().getScheduler().getCurrentTime(), arrivalCount);
		arrivalCount++;
		monArrival.record(getNode().getNetwork().getScheduler().getCurrentTime(), arrivalCount);
	}
	
	private void departureMonitor() {
		if (monDeparture == null)
			return;
		monDeparture.record(getNode().getNetwork().getScheduler().getCurrentTime(), departureCount);
		departureCount++;
		monDeparture.record(getNode().getNetwork().getScheduler().getCurrentTime(), departureCount);
	}
}
