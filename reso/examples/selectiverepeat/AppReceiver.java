package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppReceiver extends AbstractApplication{
	
	private final IPLayer ip;
    	
	public AppReceiver(IPHost host) {
		super(host, "receiver");
		ip = host.getIPLayer();
    }
	
	public void start() {
		// add protocol :
    	ip.addListener(here);
    }
	
	public void stop() {}
	
}
