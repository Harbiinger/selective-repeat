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

import reso.common.*;

public class EthernetFrame
	extends AbstractMessageWithPayload
{

	public static enum PROTO {
		IP (0x800, "IP"),
		ARP(0x806, "ARP");

		private PROTO(int number, String name) {
			this.number= number;
			this.name= name;
		}
		public final String name;
		public final int number;
	};
	
	/* Header length : MAC src (6) + MAC dst (6) + EtherType (2) + CRC (4) */
	public static final int HEADER_LEN = 18;
	public static final int MTU = 1500;
	
    public final EthernetAddress src, dst;
    public final PROTO protocol;
    public final Message payload;

    public EthernetFrame(EthernetAddress src, EthernetAddress dst, PROTO protocol, Message payload)
    	throws Exception {
    	super(HEADER_LEN, MTU, protocol.number, payload);
    	this.src= src;
    	this.dst= dst;
    	this.protocol= protocol;
    	this.payload= payload;
    }

    public String toString() {
    	return "src=" + src + ", dst=" + dst + ", proto=" + protocol.name + ", payload=[" + payload.toString() + "]";
    }
    	
}
