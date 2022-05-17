package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppSender extends AbstractApplication {
	
	private final IPLayer   ip;
	private final IPAddress dst;
	private final String    message;

	public AppSender(IPHost host, IPAddress dst, String message) {
		super(host, "sender");
		this.dst     = dst;
		this.message = message;
		ip           = host.getIPLayer();
	}

	public void start() throws Exception {
		SelectiveRepeatProtocol protocol = new SelectiveRepeatProtocol((IPHost) host);
    	ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, protocol);
		protocol.sendMessage(dst, message);
	}

	public void stop() {}

}
