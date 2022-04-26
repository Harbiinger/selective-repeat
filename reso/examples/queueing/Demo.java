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
package reso.examples.queueing;

import reso.common.Network;
import reso.ethernet.EthernetAddress;
import reso.ethernet.EthernetInterface;
import reso.examples.static_routing.AppSniffer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPRouter;
import reso.ip.IPEthernetAdapter;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.Delays;
import reso.utilities.Monitor;
import reso.utilities.MonitorIdMetric;
import reso.utilities.NetworkBuilder;

/**
 * <p>This class is used to illustrate the behaviour of a router's output queue
 * when various kinds of traffic are received and in particular when the
 * traffic intensity is high (input rate close to or above the output rate).
 * </p>
 * 
 * <p>The demo is based on a simple network topology that includes two hosts H1 end H2
 * separated by a router R1. The rate (bps) and length (m) of the links between the
 * hosts and the router can be easily configured.
 * </p>
 * 
 * <p>A packet source application is run on host H1 and a packet sink application is run on H2.
 * The packet rate and distribution H1 can be changed. Three main patterns are available
 * to date :
 * <ol>
 *   <li>packets separated by a constant interval,</li>
 *   <li>bursts of N packets separated by a constant interval, and</li>
 *   <li>packets separated by exponentially distributed intervals
 *               (i.e. leading to a Poisson process)</li>
 * </ol>
 * </p>
 *               
 * <p>The demo collects statistics about the simulation
 * <ul>
 *   <li>packet arrival and departure time at the queue on R1</li>
 *   <li>queue depth on R1</li>
 *   <li>packet end-to-end delay measured at the sink on H2</li>
 * </ul>
 * Those statistics are stored into text files that can then be used to generate
 * plots (using e.g. gnuplot).
 * </p> 
 * 
 * @author bquoitin
 *
 */
public class Demo {
	
	/** Link1 rate in bps */
	public static final long   LINK1_RATE   = 1000000;
	/** Link1 length in meters */
	public static final int    LINK1_LEN    = 0;       
	
	/** Link2 rate in bps */
	public static final int    LINK2_RATE   = 10000;
	/** Link2 length in meters */
	public static final int    LINK2_LEN    = 0;      
	
	/** Packet length in bytes */
	public static final int    PKT_LEN       = 125;
	/** Number of packets in a burst */
	public static final int    PKT_BURST_LEN = 10;    
	public static final double PKT_INTERVAL  = Delays.transmission(LINK2_RATE, PKT_LEN) * PKT_BURST_LEN;
	/** Total number of packets to generate */
	public static final int    NUM_PKTS      = -1;    
	
	/** Duration of simulation in seconds (simulated time) */
	public static final double SIMULATION_DURATION = 10;
	
	/** Display monitor events */
	public static final boolean MONITOR_DISPLAY = false; 
	
	/** Mean packet rate (in packets / second) */
	public static final double PKT_RATE= (1.0 * LINK2_RATE) / (PKT_LEN * 8);
	/** Traffic generator */
	//public static final PacketGenerator TRAFFIC_GEN= new PacketGenConstant(PKT_RATE, PKT_BURST_LEN, NUM_PKTS);
	public static final PacketGenerator TRAFFIC_GEN= new PacketGenRandomExponential(PKT_RATE, NUM_PKTS);
	
	/** Where to save results */
	public static final String RESULTS_PATH = "/tmp";
	
	/**
	 * This method displays a summary of the simulation setup and related statistics.
	 */
	public static void printSummary()
	{
		System.out.println("# LINK1  : RATE = " + LINK1_RATE + " bps ; LENGTH = " + LINK1_LEN + " m ; Tx time = " +
				Delays.transmission(LINK1_RATE, PKT_LEN) + " s");
		System.out.println("# LINK2  : RATE = " + LINK2_RATE + " bps ; LENGTH = " + LINK2_LEN + " m ; Tx time = " +
				Delays.transmission(LINK2_RATE, PKT_LEN) + " s");
		System.out.println("# SOURCE : PKT_LEN = " + (PKT_LEN * 8) + " bits ; INTERVAL = " + PKT_INTERVAL + " s");
		double avgArrivalRate= ((double) PKT_BURST_LEN) / PKT_INTERVAL;
		System.out.println("# AVG ARRIVAL RATE  = " + avgArrivalRate + "pkts/s");
		double trafficIntensity= avgArrivalRate * (PKT_LEN * 8) / LINK2_RATE;
		System.out.println("# TRAFFIC INTENSITY = " + trafficIntensity);
		if (avgArrivalRate * (PKT_LEN * 8) > LINK1_RATE)
			System.out.println("# WARNING ! The average incoming bit rate is > than the incoming link rate");
	}
	
	public static void main(String [] args)
	{
		printSummary();
		
		AbstractScheduler scheduler= new Scheduler();
		Network network= new Network(scheduler);
		try {
    		final EthernetAddress MAC_ADDR1= EthernetAddress.getByAddress("00:26:bb:4e:fc:28");
    		final EthernetAddress MAC_ADDR2= EthernetAddress.getByAddress("00:26:91:9f:a9:68");
    		final EthernetAddress MAC_ADDR3= EthernetAddress.getByAddress("00:26:91:9f:a9:69");
    		final EthernetAddress MAC_ADDR4= EthernetAddress.getByAddress("00:26:bb:4e:fc:29");
    		final IPAddress IP_ADDR1= IPAddress.getByAddress("192.168.0.1");
    		final IPAddress IP_ADDR2= IPAddress.getByAddress("192.168.0.2");
    		final IPAddress IP_ADDR3= IPAddress.getByAddress("192.168.1.1");
    		final IPAddress IP_ADDR4= IPAddress.getByAddress("192.168.1.2");

    		IPHost host1= NetworkBuilder.createHost(network, "H1", IP_ADDR1, MAC_ADDR1);
    		host1.getIPLayer().addRoute(IP_ADDR2, "eth0");
    		//host1.addApplication(new AppSniffer(host1, new String [] { "eth0" }));
    		System.out.println("Mean packet rate = " + PKT_RATE + "/ s");
    		host1.addApplication(new AppPacketSource(host1, IP_ADDR4, PKT_LEN, TRAFFIC_GEN));
    		
    		IPHost host2= NetworkBuilder.createHost(network, "H2", IP_ADDR4, MAC_ADDR4);
    		host2.getIPLayer().addRoute(IP_ADDR3, "eth0");
    		//host2.addApplication(new AppSniffer(host2, new String [] { "eth0" }));
    		MonitorIdMetric<Double> monDelay= new MonitorIdMetric<Double>("e2e-delay", "H2", MONITOR_DISPLAY);
    		host2.addApplication(new AppPacketSink(host2, monDelay));
    		
    		IPRouter router= NetworkBuilder.createRouter(network, "R1",
    				new IPAddress [] { IP_ADDR2, IP_ADDR3 },
    				new EthernetAddress [] { MAC_ADDR2, MAC_ADDR3 });
    		
    		NetworkBuilder.createLink(host1, "eth0", router, "eth0", LINK1_LEN, LINK1_RATE);
    		NetworkBuilder.createLink(router, "eth1", host2, "eth0", LINK2_LEN, LINK2_RATE);

    		// Add static routes
    		host1.getIPLayer().addRoute(IP_ADDR4, IP_ADDR2);
    		host2.getIPLayer().addRoute(IP_ADDR1, IP_ADDR3);
    		router.getIPLayer().addRoute(IP_ADDR4, "eth1");
    		router.getIPLayer().addRoute(IP_ADDR1, "eth0");
    		
    		// Add static ARP entries in H1 and R to avoid additional delays
    		((IPEthernetAdapter) host1.getIPLayer().getInterfaceByName("eth0")).addARPEntry(IP_ADDR2, MAC_ADDR2);
    		((IPEthernetAdapter) router.getIPLayer().getInterfaceByName("eth1")).addARPEntry(IP_ADDR4, MAC_ADDR4);
    		
    		// Monitor R1.eth1's queue depth
    		Monitor monQueue= ((EthernetInterface) router.getInterfaceByName("eth1")).getQueueMonitor(MONITOR_DISPLAY);
    		Monitor monArrival= ((EthernetInterface) router.getInterfaceByName("eth1")).getArrivalMonitor(MONITOR_DISPLAY);
    		Monitor monDeparture= ((EthernetInterface) router.getInterfaceByName("eth1")).getDepartureMonitor(MONITOR_DISPLAY);
    		
    		host1.start();
    		host2.start();
    		
    		scheduler.runUntil(SIMULATION_DURATION);
    		
    		monQueue.save(RESULTS_PATH + "/queue.dat");
    		monArrival.save(RESULTS_PATH + "/arrival.dat");
    		monDeparture.save(RESULTS_PATH + "/departure.dat");
    		monDelay.save(RESULTS_PATH + "/delay.dat");
    		
    	} catch (Exception e) {
    		System.err.println(e.getMessage());
    		e.printStackTrace(System.err);
    	}
	}

}
