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

import reso.common.AbstractMessageWithPayload;
import reso.common.Message;
import reso.common.ProtocolTable;

public class Datagram
extends AbstractMessageWithPayload
{

	private final static ProtocolTable protocols= new ProtocolTable();
	
	/* Header length :
	 *  version (1/2), header length (1/2), TOS (1), length (2)
	 *  ID (2), flags (3/8), offset (13/8),
	 * TTL (1), protocol (1), checksum (2),
	 * IP src (4),
	 * IP dst (4)
	 */ 
	public static final int HEADER_LEN = 20;
	public static final int MAX_PAYLOAD_LEN = 65535 - HEADER_LEN;
	
    public final IPAddress src, dst;
    private int ttl;

    public Datagram(IPAddress src, IPAddress dst, int protocol,
		    int ttl, Message payload)
    	throws Exception {
    	super(HEADER_LEN, MAX_PAYLOAD_LEN, protocol, payload);
    	this.src= src;
    	this.dst= dst;
    	this.ttl= ttl;
    	protocols.checkAllocated(protocol);
    }

    public int getTTL() {
    	return ttl;
    }

    public void decTTL() {
    	ttl-= 1;
    }

    public String toString() {
    	return "src=" + src + ", dst=" + dst + ",proto=" + getProtocol() +
    		", payload=[" + getPayload() + "]";
    }
    
	public static int allocateProtocolNumber(String name) {
		return protocols.allocateProtocolNumber(name);
	}

}
