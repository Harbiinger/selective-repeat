package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppSender extends AbstractApplication {
	
	private final IPLayer   ip;
	private final IPAddress dst;
	private final int       num;

	public AppSender(IPHost host, IPAddress dst, int num) {
		super(host, "sender");
		this.dst = dst;
		this.num = num;
		ip       = host.getIPLayer();
	}

	public void start() throws Exception {
		// I guess we have to add our protocol here :
		ip.addListener(>PROTOCOL<);
	}

	public void stop() {
	}

}
