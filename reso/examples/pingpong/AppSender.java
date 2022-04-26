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

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppSender
    extends AbstractApplication
{ 
	
	private final IPLayer ip;
    private final IPAddress dst;
    private final int num;

    public AppSender(IPHost host, IPAddress dst, int num) {	
    	super(host, "sender");
    	this.dst= dst;
    	this.num= num;
    	ip= host.getIPLayer();
    }

    public void start()
    throws Exception {
    	ip.addListener(PingPongProtocol.IP_PROTO_PINGPONG, new PingPongProtocol((IPHost) host));
    	ip.send(IPAddress.ANY, dst, PingPongProtocol.IP_PROTO_PINGPONG, new PingPongMessage(num));
    }
    
    public void stop() {}
    
}

