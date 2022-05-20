package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppReceiver extends AbstractApplication{
	
	private final IPLayer ip;
	private final int     packetLoss;
    	
	public AppReceiver(IPHost host, int packetLoss) {
		super(host, "receiver");
		this.packetLoss = packetLoss;
		ip = host.getIPLayer();
    }
	
	public void start() {
		ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatProtocol((IPHost) host, "receiver", packetLoss));
    }
	
	public void stop() {}
	
}
