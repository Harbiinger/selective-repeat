package reso.utilities;

import java.util.HashSet;

import reso.common.HardwareInterface;
import reso.common.Network;
import reso.common.Node;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPLoopbackAdapter;
import reso.ip.IPRouteEntry;
import reso.ip.IPRouter;

public class FIBDumper {

	private static void dumpRouteEntry(IPRouteEntry re, IPHost h) {
		System.out.println("\t" + re);
		if (re.oif != null) {
			System.out.println("\t\tactive:" + re.oif.isActive() +
					", cost:" + re.oif.getMetric());
			if (re.oif instanceof IPLoopbackAdapter)
				return;
			if (re.oif instanceof IPInterfaceAdapter) {
				String name= re.oif.getName();
				HardwareInterface<?> iface= h.getInterfaceByName(name);
				System.out.print("\t\tactive:" + iface.isActive());
				if (iface.isConnected()) {
					System.out.println(", connected:true, to:" +
							iface.getLink().getTail().getNode().name);
				} else
					System.out.println(", connected:false");
			}
		}
	}
	
	public static void dumpForHost(IPHost h) {
		dumpForHost(h, null);
	}
	
	public static void dumpForHost(IPHost h, HashSet<IPAddress> dsts) {
		System.out.println("[" + h.name + "]");
		for (IPRouteEntry re: h.getIPLayer().getRoutes()) {
			if ((dsts == null) || dsts.contains(re.dst))
				dumpRouteEntry(re, h);
		}
	}
	
	public static void dumpForAllRouters(Network network) {
		dumpForAllRouters(network, null);
	}

	public static void dumpForAllRouters(Network network, HashSet<IPAddress> dsts) {
		for (Node n: network.getNodes()) {
			if (!(n instanceof IPRouter))
				continue;
			FIBDumper.dumpForHost((IPRouter) n, dsts);
		}
	}
	
}
