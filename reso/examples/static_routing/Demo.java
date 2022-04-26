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
package reso.examples.static_routing;

import reso.common.Link;
import reso.common.Network;
import reso.ethernet.EthernetAddress;
import reso.ethernet.EthernetFrame;
import reso.ethernet.EthernetInterface;
import reso.examples.pingpong.AppReceiver;
import reso.examples.pingpong.AppSender;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPRouter;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.NetworkBuilder;

public class Demo {
	
	/* This demo shows how to create a 3 node topology: 2 nodes separated by a single router.
	 * Both hosts will use the ping-pong protocol demonstrated in reso.examples.pingpong.
	 * To allow both hosts to reach each other, static routes are configured in each host and
	 * in the router.
	 * 
	 * Note: although the propagation of each link is only 25 ms, the first two messages sent
	 * will each require 150ms. This is due to ARP query/response exchanges, as discussed in
	 * the ping-pong protocol example (see reso.examples.pingpong).
	 */
	public static void main(String [] args)
	{
		AbstractScheduler scheduler= new Scheduler();
		Network network= new Network(scheduler);
		try {
    		final EthernetAddress MAC_ADDR1= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x28);
    		final EthernetAddress MAC_ADDR2= EthernetAddress.getByAddress(0x00, 0x26, 0x91, 0x9f, 0xa9, 0x68);
    		final EthernetAddress MAC_ADDR3= EthernetAddress.getByAddress(0x00, 0x26, 0x91, 0x9f, 0xa9, 0x69);
    		final EthernetAddress MAC_ADDR4= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x29);
    		final IPAddress IP_ADDR1= IPAddress.getByAddress(192, 168, 0, 1);
    		final IPAddress IP_ADDR2= IPAddress.getByAddress(192, 168, 0, 2);
    		final IPAddress IP_ADDR3= IPAddress.getByAddress(192, 168, 1, 1);
    		final IPAddress IP_ADDR4= IPAddress.getByAddress(192, 168, 1, 2);

    		IPHost host1= NetworkBuilder.createHost(network, "H1", IP_ADDR1, MAC_ADDR1);
    		host1.getIPLayer().addRoute(IP_ADDR2, "eth0");
    		host1.addApplication(new AppSniffer(host1, new String [] { "eth0" }));
    		host1.addApplication(new AppSender(host1, IP_ADDR4, 5));
    		
    		IPHost host2= NetworkBuilder.createHost(network, "H2", IP_ADDR4, MAC_ADDR4);
    		host2.getIPLayer().addRoute(IP_ADDR3, "eth0");
    		host2.addApplication(new AppReceiver(host2));
    		
    		IPRouter router= NetworkBuilder.createRouter(network, "R1",
    				new IPAddress [] { IP_ADDR2, IP_ADDR3 },
    				new EthernetAddress [] { MAC_ADDR2, MAC_ADDR3 });
    		
    		new Link<EthernetFrame>((EthernetInterface) host1.getInterfaceByName("eth0"),
    				(EthernetInterface) router.getInterfaceByName("eth0"), 5000000, 100000);
    		new Link<EthernetFrame>((EthernetInterface) router.getInterfaceByName("eth1"),
    				(EthernetInterface) host2.getInterfaceByName("eth0"), 5000000, 100000);

    		// Add static routes
    		host1.getIPLayer().addRoute(IP_ADDR4, IP_ADDR2);
    		host2.getIPLayer().addRoute(IP_ADDR1, IP_ADDR3);
    		router.getIPLayer().addRoute(IP_ADDR4, "eth1");
    		router.getIPLayer().addRoute(IP_ADDR1, "eth0");
    		
    		host1.start();
    		host2.start();
    		
    		scheduler.run();
    		
    	} catch (Exception e) {
    		System.err.println(e.getMessage());
    		e.printStackTrace(System.err);
    	}
	}

}
