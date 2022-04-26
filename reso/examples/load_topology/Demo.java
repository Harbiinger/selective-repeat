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
package reso.examples.load_topology;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

import reso.common.HardwareInterface;
import reso.common.Network;
import reso.common.Node;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.NetworkBuilder;
import reso.utilities.NetworkGrapher;

public class Demo {

	public static final String TOPO_FILE= "reso/data/topology.txt";
	
	/* This demo shows how to load a complete topology from a text file.
	 * One a topology file has been loaded and routers/links have been instantiated,
	 * the main program will dump information about the whole topology.
	 */
	public static void main(String [] args) {
		String filename= Demo.class.getClassLoader().getResource(TOPO_FILE).getFile();
		AbstractScheduler scheduler= new Scheduler();
		try {
			Network network= NetworkBuilder.loadTopology(filename, scheduler);
			for (Node n: network.getNodes()) {
				System.out.println(n);
				for (HardwareInterface<?> iface: n.getInterfaces()) {
					System.out.print("\t" + iface);
					if (iface instanceof IPInterfaceAdapter)
						System.out.print("\t" + ((IPInterfaceAdapter) iface).getAddress());
					if (iface.isConnected())
						System.out.print("\tconnected");
					System.out.println();
				}
			}
			
			((IPHost) network.getNodeByName("R1")).getInterfaceByName("eth2").down();
			
			File f= new File("/tmp/topology.graphviz");
			Writer w= new BufferedWriter(new FileWriter(f));
			NetworkGrapher.toGraphviz(network, new PrintWriter(w));
			w.close();
						
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	
}
