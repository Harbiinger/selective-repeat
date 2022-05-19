package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppReceiver extends AbstractApplication{
	
	private final IPLayer ip;
	private final int     packetLoss;
	private SelectiveRepeatProtocol protocol;
    	
	public AppReceiver(IPHost host, int packetLoss) {
		super(host, "receiver");
		this.packetLoss = packetLoss;
		ip = host.getIPLayer();
    }
	
	public void start() {
		protocol = new SelectiveRepeatProtocol((IPHost) host, "alice", packetLoss);
		ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, protocol);
    }

	public void stop() {
		System.out.println(protocol.getData());
	}
	
}
