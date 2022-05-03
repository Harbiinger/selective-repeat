package reso.examples.selectiverepeat;

import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

	private final IPHost host;
	private final int    windowSize;

	public SelectiveRepeatProtocol(IPHost host) {
		this.host = host;
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		// our protocol code 
	}

}
