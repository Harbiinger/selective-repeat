package reso.examples.selectiverepeat;

import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_SELECTIVE_REPEAT= Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");


	private final IPHost host;
	private final int windowSize;

	public SelectiveRepeatProtocol(IPHost host) {
		this.host = host;
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		// TODO : our protocol code 
	}

}
