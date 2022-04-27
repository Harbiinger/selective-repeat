package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;
import reso.ip.IPAddress;

public class App extends AbstractApplication {
	
	private final IPLayer ip;

	public App(IPHost host, IPAdress dst, int num) {
		super(host, "app");
		ip       = host.getIPLayer();
		this.dst = dst;
		this.num = num;
	}

	public void start() {
		// we have to add our selective repeat protocol here I guess
	}

	public void stop() {}

}
