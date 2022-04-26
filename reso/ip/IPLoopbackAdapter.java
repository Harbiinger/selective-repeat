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

public class IPLoopbackAdapter
extends AbstractIPInterfaceAdapter
{
	
	private final IPLayer ip;
	
	public IPLoopbackAdapter(IPLayer ip, int index)
	throws Exception {
		super(ip, "lo");
		setIndex(index);
		this.ip= ip;
	}
	
    public void send(Datagram datagram, IPAddress gateway)
    throws Exception {
    	receive(datagram);
    }

	public void receive(Datagram datagram)
	throws Exception {
    	ip.receive(this, datagram);
	}
		
}
