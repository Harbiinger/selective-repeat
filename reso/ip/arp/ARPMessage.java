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

import reso.common.Message;
import reso.ethernet.EthernetAddress;
import reso.ip.IPAddress;

public class ARPMessage
implements Message {

	public static enum Type {
		REQUEST, RESPONSE
	}
	
	public final Type type;
	public final IPAddress ipAddr;
	public final EthernetAddress ethAddr;
	
	public static final int HEADER_LEN = 28;
	
	private ARPMessage(Type type, IPAddress addr, EthernetAddress maddr) {
		this.type= type;
		this.ipAddr= addr;
		this.ethAddr= maddr;
	}
	
	public static ARPMessage request(IPAddress addr) {
		return new ARPMessage(Type.REQUEST, addr, null);
	}
	
	public static ARPMessage response(IPAddress addr, EthernetAddress maddr) {
		return new ARPMessage(Type.RESPONSE, addr, maddr);
	}
	
	public String toString() {
		String s= "type=" + type + ", ip=" + ipAddr;
		if (type == Type.RESPONSE)
			s+= ", MAC=" + ethAddr;
		return s;
	}

	@Override
	public int getByteLength() {
		// Hardware Type (2), Protocol Type (2),
		// Hardware Address Length (1), Protocol Address Length (1), Opcode (2)
		// Sender Hardware Address (6 for Ethernet)
		// Sender Protocol Address (4 for IPv4)
		// Target Hardware Address (6 for Ethernet)
		// Target Protocol Address (4 for IPv4)
		return HEADER_LEN;
	}
	
}
