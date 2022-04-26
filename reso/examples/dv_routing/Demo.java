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
package reso.examples.dv_routing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

import reso.common.Network;
import reso.common.Node;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPLayer;
import reso.ip.IPRouter;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.FIBDumper;
import reso.utilities.NetworkBuilder;
import reso.utilities.NetworkGrapher;

public class Demo {
	
	public static final String TOPO_FILE= "reso/data/topology.txt";
	
	private static IPAddress getRouterID(IPLayer ip) {
		IPAddress routerID= null;
		for (IPInterfaceAdapter iface: ip.getInterfaces()) {
			IPAddress addr= iface.getAddress();
			if (routerID == null)
				routerID= addr;
			else if (routerID.compareTo(addr) < 0)
				routerID= addr;
		}
		return routerID;
	}
	
	public static void main(String [] args) {
		String filename= Demo.class.getClassLoader().getResource(TOPO_FILE).getFile();
		
		AbstractScheduler scheduler= new Scheduler();
		try {
			Network network= NetworkBuilder.loadTopology(filename, scheduler);
			
			// Add routing protocol application to each router
			for (Node n: network.getNodes()) {
				if (!(n instanceof IPRouter))
					continue;
				IPRouter router= (IPRouter) n;
				boolean advertise= true;
				//boolean advertise= (n.name.equals("R4"));
				router.addApplication(new DVRoutingProtocol(router, advertise));
				router.start();
			}
			
			// Run simulation
			scheduler.run();
			
			// Display forwarding table for each node
			FIBDumper.dumpForAllRouters(network);			
			
			for (Node n: network.getNodes()) {
				//IPAddress ndst= ((IPHost) n).getIPLayer().getInterfaceByName("lo0").getAddress();
				IPAddress ndst= getRouterID(((IPHost) n).getIPLayer());
				File f= new File("/tmp/topology-routing-" + n.name + ".graphviz");
				System.out.println("Writing file "+f);
				Writer w= new BufferedWriter(new FileWriter(f));
				NetworkGrapher.toGraphviz2(network, ndst, new PrintWriter(w));
				w.close();
			}
			
			((IPHost) network.getNodeByName("R3")).getIPLayer().getInterfaceByName("eth0").setMetric(200);
			((IPHost) network.getNodeByName("R3")).getIPLayer().getInterfaceByName("eth0").down();
			((IPHost) network.getNodeByName("R3")).getIPLayer().getInterfaceByName("eth0").up();			
			network.getNodeByName("R3").getInterfaceByName("eth0").down();
			network.getNodeByName("R3").getInterfaceByName("eth0").up();
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	
}
