package reso.examples.selectiverepeat;

import reso.common.AbstractTimer;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
import reso.scheduler.AbstractScheduler;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_SELECTIVE_REPEAT= Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");


	private final IPHost host;
	private final int windowSize;
	private AbstractTimer timer;


	public SelectiveRepeatProtocol(IPHost host) {
		this.host = host;
		double interval = 5.0;
		windowSize = 2;
		timer= new MyTimer(host.getNetwork().getScheduler(), interval);
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		// TODO : our protocol code
		
	}


    private class MyTimer extends AbstractTimer {
    	public MyTimer(AbstractScheduler scheduler, double interval) {
    		super(scheduler, interval, false);
    	}
    	protected void run() throws Exception {
			System.out.println("app=[" + host.getIPLayer() + "]" +
					" time=" + scheduler.getCurrentTime());
		}
    }
    
    public void start()
    throws Exception {
    	timer.start();
    }
    
    public void stop() {
    	timer.stop();
    }
}
