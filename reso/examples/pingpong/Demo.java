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

import reso.common.*;
import reso.ethernet.*;
import reso.examples.static_routing.AppSniffer;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.NetworkBuilder;

public class Demo
{

	/* Enable or disable packet capture (can be used to observe ARP messages) */
	private static final boolean ENABLE_SNIFFER= false;
	
	/* A message is sent from the sender to the receiver with a counter initialized to 5.
	 * When the receiver get the message it decreases the counter and send the message back
	 * to the sender. Sender/receiver do this until a message is received with a counter
	 * equal to 0.
	 * 
	 * Each time the sender/receiver gets a message it will write a message on the console.
	 * The length of the link between the two hosts is 5000km long, which gives a propagation
	 * delay of 0.025 seconds.
	 * 
	 *  The console should show 6 messages. The first message appears after 0.075 seconds
	 *  as a first RTT is required for the first ARP query/response exchange initiated by H1.
	 *  The second message appears after 0.15 seconds as there is also an ARP query/response
	 *  initiated by host 2. The third message appears after 0.175 seconds and further messages
	 *  are spaced by 0.025 seconds (only the propagation delay). 
	 */
    public static void main(String [] args) {
		AbstractScheduler scheduler= new Scheduler();
		Network network= new Network(scheduler);
    	try {
    		final EthernetAddress MAC_ADDR1= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x28);
    		final EthernetAddress MAC_ADDR2= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x29);
    		final IPAddress IP_ADDR1= IPAddress.getByAddress(192, 168, 0, 1);
    		final IPAddress IP_ADDR2= IPAddress.getByAddress(192, 168, 0, 2);

    		IPHost host1= NetworkBuilder.createHost(network, "H1", IP_ADDR1, MAC_ADDR1);
    		host1.getIPLayer().addRoute(IP_ADDR2, "eth0");
    		if (ENABLE_SNIFFER)
    			host1.addApplication(new AppSniffer(host1, new String [] {"eth0"}));
    		host1.addApplication(new AppSender(host1, IP_ADDR2, 5));

    		IPHost host2= NetworkBuilder.createHost(network,"H2", IP_ADDR2, MAC_ADDR2);
    		host2.getIPLayer().addRoute(IP_ADDR1, "eth0");
    		host2.addApplication(new AppReceiver(host2));

    		EthernetInterface h1_eth0= (EthernetInterface) host1.getInterfaceByName("eth0");
    		EthernetInterface h2_eth0= (EthernetInterface) host2.getInterfaceByName("eth0");
    		
    		// Connect both interfaces with a 5000km long link
    		new Link<EthernetFrame>(h1_eth0, h2_eth0, 5000000, 100000);

    		host1.start();
    		host2.start();
    		
    		scheduler.run();
    	} catch (Exception e) {
    		System.err.println(e.getMessage());
    		e.printStackTrace(System.err);
    	}
    }

}
