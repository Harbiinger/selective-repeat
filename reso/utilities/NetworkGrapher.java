package reso.utilities;

import java.io.PrintWriter;

import reso.common.HardwareInterface;
import reso.common.Link;
import reso.common.Message;
import reso.common.Network;
import reso.common.Node;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPRouteEntry;

/**
 * This class can be used to generate text file representations of the network graph
 * in AT&T's dot language. Those text files can then be converted to a graph image
 * in <code>.png</code>, <code>.eps</code> or <code>.pdf</code> formats using one of the graphviz tools
 * (<code>neato</code> and <code>fdp</code> are the preferred ones).
 * 
 * @see <a href="http://www.graphviz.org">Graphviz website</a>
 */

public class NetworkGrapher {

	/** Font size for edge labels (default=14 points) */
	public static final int EDGE_LABEL_FONT_SIZE= 10;
	/** Distance between edge and label (default=10 points) */
	public static final double EDGE_LABEL_DISTANCE= 1;
	/** Shape of nodes */
	public static final String NODE_SHAPE= "circle";
	/** Font size of node labels */
	public static final int NODE_LABEL_FONT_SIZE= 11;
	/** (default=1.0) */
	public static final double EDGE_LENGTH= 2;
	
	/**
	 * This method generates a representation of a network in graphviz format.
	 * The representation contains hosts, routers, links and IGP metrics.
	 * 
	 * @param network is the network to represent.
	 * @param pw is a PrintWriter instance to which the textual representation will be written.
	 */
	public static void toGraphviz(Network network, PrintWriter pw) {
		pw.println("graph \"reso\" {");
		pw.print("  graph [");
		pw.print("outputorder=nodesfirst");
		pw.print(", overlap=\"scale\"");
		pw.print(", mindist=" + 5);
		pw.print(", K=" + 1);
		pw.println("] ;");
		pw.print("  edge [");
		pw.print("labelfontsize=" + EDGE_LABEL_FONT_SIZE);
		pw.print(", fontsize=" + 8);
		pw.print(", labeldistance=" + EDGE_LABEL_DISTANCE);
		//pw.print(", labelangle=" + 30);
		pw.print(", len=" + EDGE_LENGTH);
		pw.println("] ;");
		pw.print("  node [");
		pw.print("fontsize=" + NODE_LABEL_FONT_SIZE);
		pw.println("] ;");
		
		for (Node n: network.getNodes()) {
			pw.print("  \"" + n.name + "\" [");
			pw.print("shape=" + NODE_SHAPE);
			pw.println("] ;");
		}
		
		for (Node n: network.getNodes()) {
			for (HardwareInterface<?> iface: n.getInterfaces()) {
				Link<?> l= iface.getLink();
				HardwareInterface<?> hi= l.getHead(); 
				Node hn= hi.getNode();
				HardwareInterface<?> ti= l.getTail();
				Node tn= ti.getNode();

				if (tn == n)
					continue;
				
				if (hn.name.compareTo(tn.name) > 0) {
					HardwareInterface<? extends Message> tmpi= hi;
					hi= ti;
					ti= tmpi;
					Node tmpn= hn;
					hn= tn;
					tn= tmpn;
				}
				
				int hmetric= getMetric(hn, hi);
				int tmetric= getMetric(tn, ti);
					
				// Note: with undirected edges, graphviz will consider the left node as the tail and
				// the right node as the head. It's a bit confusing... but that's the convention we need
				// to follow
				pw.print("  \"" + tn.name + "\" -- \"" + hn.name + "\" [");
				pw.print("headlabel=\"" + hi.getName() + (hi.isActive()?"":" (down)") + "\"");
				pw.print(", taillabel=\"" + ti.getName() + (ti.isActive()?"":" (down)") + "\"");
				pw.print(", label=\"(" + ((hmetric == Integer.MAX_VALUE)?"?":hmetric) + "/" + 
						((tmetric == Integer.MAX_VALUE)?"?":tmetric)+ ")\"");
				pw.println("] ;");
				
			}
		}
		pw.println("}");
	}
	
	/**
	 * This method generates a representation of a network in graphviz format.
	 * The representation contains hosts, routers, links and the routes towards
	 * a specific destination.
	 * 
	 * @param network is the network to represent.
	 * @param dst is the address of the routes' destination.
	 * @param pw is a PrintWriter instance to which the textual representation will be written.
	 */
	public static void toGraphviz2(Network network, IPAddress dst, PrintWriter pw) {
		pw.println("graph \"reso\" {");
		pw.print("  graph [");
		pw.print("outputorder=nodesfirst");
		pw.print(", overlap=\"scale\"");
		pw.print(", mindist=" + 5);
		pw.print(", K=" + 1);
		pw.println("] ;");
		pw.print("  edge [");
		pw.print("labelfontsize=" + EDGE_LABEL_FONT_SIZE);
		pw.print(", fontsize=" + 8);
		pw.print(", labeldistance=" + EDGE_LABEL_DISTANCE);
		//pw.print(", labelangle=" + 30);
		pw.print(", len=" + EDGE_LENGTH);
		pw.println("] ;");
		pw.print("  node [");
		pw.print("fontsize=" + NODE_LABEL_FONT_SIZE);
		pw.println("] ;");
		
		for (Node n: network.getNodes()) {
			pw.print("  \"" + n.name + "\" [");
			if (((IPHost) n).getIPLayer().hasAddress(dst))
				pw.print("shape=" + "doublecircle");
			else
				pw.print("shape=" + NODE_SHAPE);
			pw.println("] ;");
		}
		
		for (Node n: network.getNodes()) {
			for (HardwareInterface<?> iface: n.getInterfaces()) {
				Link<?> l= iface.getLink();
				HardwareInterface<?> hi= l.getHead(); 
				Node hn= hi.getNode();
				HardwareInterface<?> ti= l.getTail();
				Node tn= ti.getNode();

				if (tn == n)
					continue;
				
				if (hn.name.compareTo(tn.name) > 0) {
					HardwareInterface<?> tmpi= hi;
					hi= ti;
					ti= tmpi;
					Node tmpn= hn;
					hn= tn;
					tn= tmpn;
				}
				
				IPRouteEntry hre= getRoute(hn, dst);
				IPRouteEntry tre= getRoute(tn, dst);
						
				// Note: with undirected edges, graphviz will consider the left node as the tail and
				// the right node as the head. It's a bit confusing... but that's the convention we need
				// to follow
				pw.print("  \"" + tn.name + "\" -- \"" + hn.name + "\" [");
				pw.print("headlabel=\"" + hi.getName() + (hi.isActive()?"":" (down)") + "\"");
				pw.print(", taillabel=\"" + ti.getName() + (ti.isActive()?"":" (down)") + "\"");
				pw.print(", dir=\"both\"");

				boolean large= false;
				if ((hre != null) && (hre.oif.getName().equals(hi.getName()))) {
					large= true;
					pw.print(", arrowtail=empty");
				} else
					pw.print(", arrowtail=none");
				if ((tre != null) && (tre.oif.getName().equals(ti.getName()))) {
					large= true;
					pw.print(", arrowhead=empty");
				} else
					pw.print(", arrowhead=none");
				if (large)
					pw.print(", penwidth=3");
				pw.println("] ;");
				
			}
		}
		pw.println("}");
	}
	
	private static int getMetric(Node node, HardwareInterface<?> iface) {
		if (!(node instanceof IPHost))
			return Integer.MAX_VALUE;
		IPHost ip_node= (IPHost) node;
		IPInterfaceAdapter ip_iface= ip_node.getIPLayer().getInterfaceByName(iface.getName());
		return ip_iface.getMetric();
	}
	
	private static IPRouteEntry getRoute(Node n, IPAddress dst) {
		if (!(n instanceof IPHost))
			return null;
		return ((IPHost) n).getIPLayer().getRouteTo(dst);
	}
	
}
