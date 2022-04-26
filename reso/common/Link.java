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

import reso.scheduler.AbstractScheduler;
import reso.scheduler.SchedulerException;
import reso.utilities.Delays;

public class Link<M extends Message>
	implements MessageListener<M>
{
	
	public static final double SPEED_OF_LIGHT  = 300000000;
	public static final double WIRE_PROP_SPEED = (2 * SPEED_OF_LIGHT) / 3; /* Propagation speed in copper (assumed to be 2/3 of c) */
	
	private /*final*/ HardwareInterface<M> iface1, iface2;
	public final double length; // Link length in meters
	public final long bitRate; // Link bitrate in bits/second
	
	public Link(double length, long bitRate) {
		this.length= length;
		this.bitRate= bitRate;
	}
	
	public Link(HardwareInterface<M> iface1, HardwareInterface<M> iface2, double length, long bitRate)
	throws Exception {
		this(length, bitRate);
		this.iface1= iface1;
		this.iface2= iface2;
		iface1.connectTo(this);
		iface2.connectTo(this);
	}
	
	public boolean isConnectedTo(HardwareInterface<M> iface) {
		return (iface1 == iface) || (iface2 == iface);
	}
	
	public void send(HardwareInterface<M> src, M msg)
	throws SchedulerException {
		AbstractScheduler scheduler= src.getNode().getNetwork().getScheduler();
    	scheduler.schedule(new EventMessageSend<M>(scheduler.getCurrentTime() + getPropagationDelay(), msg, src, this));
	}

	public void receive(HardwareInterface<M> iface, M msg)
	throws Exception {
		if (iface == iface1)
			iface2.receive(msg);
		else
			iface1.receive(msg);
	}
	
	public double getTransmissionDelay(int byteLength) {
		return Delays.transmission(bitRate, byteLength);
	}
	
	public double getPropagationDelay() {
		return Delays.propagation(length, WIRE_PROP_SPEED);
	}
	
	public HardwareInterface<M> getHead() {
		return iface1;
	}

	public HardwareInterface<M> getTail() {
		return iface2;
	}

	public long getBitRate() {
		return bitRate;
	}

}
