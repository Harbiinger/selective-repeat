package reso.examples.arp;

import reso.common.AbstractApplication;
import reso.common.Network;
import reso.ethernet.EthernetAddress;
import reso.examples.queueing.AppPacketSource;
import reso.examples.queueing.PacketGenConstant;
import reso.examples.static_routing.AppSniffer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.scheduler.AbstractEvent;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.NetworkBuilder;

/**
 * The objective of this class is to demonstrate the ARP protocol.
 * 
 * <ol>
 *   <li>Try to resolve an existing destination</li>
 *   <li>Try to resolve an unknown destination</li>
 *   <li>Wait for an ARP entry to age and timeout, then resolve again</li>
 * </ol>
 * 
 * @author bquoitin
 */
public class Demo {
	
	/** Packet length (in bytes) */
	public static final int PKT_LEN    = 125;
	/** Link bit rate (in bps) */
	public static final long LINK_RATE = 100000;
	/** Link length (in meters) */
	public static final int LINK_LEN   = 100;
	
	/** Duration of simulation (in seconds, simulated time) */
	public static final int SIMULATION_DURATION= 20;
	
	/**
	 * This is where everything starts...
	 * 
	 * @param args
	 */
	public static void main(String [] args) {
		AbstractScheduler scheduler= new Scheduler(false);
		Network network= new Network(scheduler);
		try {
			final EthernetAddress MAC_ADDR1= EthernetAddress.getByAddress("00:26:bb:4e:fc:28");
			final EthernetAddress MAC_ADDR2= EthernetAddress.getByAddress("00:26:91:9f:a9:68");
    		final IPAddress IP_ADDR1= IPAddress.getByAddress("192.168.0.1");
    		final IPAddress IP_ADDR2= IPAddress.getByAddress("192.168.0.2");
    		final IPAddress IP_ADDR3= IPAddress.getByAddress("192.168.0.3");

    		final IPHost host1= NetworkBuilder.createHost(network, "H1", IP_ADDR1, MAC_ADDR1);
    		host1.addApplication(new AppSniffer(host1, new String [] {"eth0"}));
    		host1.addApplication(new AppPacketSource(host1, IP_ADDR2, PKT_LEN, new PacketGenConstant(1000, 0, 1)));
    		host1.addApplication(new AppPacketSource(host1, IP_ADDR3, PKT_LEN, new PacketGenConstant(1000, 0, 1)));
    		host1.getIPLayer().addRoute(IP_ADDR2, "eth0");
    		host1.getIPLayer().addRoute(IP_ADDR3, "eth0");
    		
    		IPHost host2= NetworkBuilder.createHost(network, "H2", IP_ADDR2, MAC_ADDR2);
    		
    		NetworkBuilder.createLink(host1, "eth0", host2, "eth0", LINK_LEN, LINK_RATE);
    		
    		host1.start();
    		host2.start();
    		
    		scheduler.schedule(new AbstractEvent(10.0) {
				public void run() throws Exception {
					AbstractApplication app= new AppPacketSource(host1, IP_ADDR2, PKT_LEN, new PacketGenConstant(1000, 0, 1));
					host1.addApplication(app);
					app.start();
				}   			
    		});

			scheduler.run();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}
