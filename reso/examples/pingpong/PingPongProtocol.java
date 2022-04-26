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
package reso.examples.pingpong;

import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class PingPongProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_PINGPONG= Datagram.allocateProtocolNumber("PING-PONG");
	
	private final IPHost host; 
	
	public PingPongProtocol(IPHost host) {
		this.host= host;
	}
	
	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
    	PingPongMessage msg= (PingPongMessage) datagram.getPayload();
		System.out.println("Ping-Pong (" + (int) (host.getNetwork().getScheduler().getCurrentTime()*1000) + "ms)" +
				" host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
				datagram.dst + ", iif=" + src + ", counter=" + msg.num);
    	if (msg.num > 0)
    		host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_PINGPONG, new PingPongMessage(msg.num-1));
	}

}
