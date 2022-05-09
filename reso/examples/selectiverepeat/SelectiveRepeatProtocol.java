package reso.examples.selectiverepeat;

import reso.common.AbstractTimer;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
import reso.ip.IPLayer;
import reso.scheduler.AbstractScheduler;

import java.util.ArrayList;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_SELECTIVE_REPEAT= Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");


	private final IPHost        host;
	private final int           segmentSize;
	private       IPAddress     dst;
	private       AbstractTimer timer;
	private       int           windowSize;
	private       int           sendBase;   // sequence number of the fist packet in the window
	private       int           seqNum;
	private ArrayList<String>   messagesList = new ArrayList();


	public SelectiveRepeatProtocol(IPHost host, int segmentSize) {
		this.host = host;
		this.segmentSize = segmentSize;
		double interval = 5.0;
		windowSize = 1;
		sendBase = 0;
		seqNum = 0;
		timer= new MyTimer(host.getNetwork().getScheduler(), interval);
	}

	/*
	 * Method used by an application to send a message to the given host.
	 * @param dst : the destination host
	 * @param message : the message
	 */
	public void sendMessage(IPAddress dst, String message) throws Exception {
		this.dst = dst;

		// message is devided in multiple smaller messages
		int i = 0;
		while (i+segmentSize < message.length()) {
			messagesList.add(message.substring(i, i+segmentSize));
			i += segmentSize;
		}
		messagesList.add(message.substring(i, message.length()));
		send(messagesList.get(0), false);
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		SelectiveRepeatSegment payload = (SelectiveRepeatSegment) datagram.getPayload();
		System.out.println(payload);
		if (payload.acked) {
			System.out.println(payload);
			send(messagesList.get(++sendBase), false);	
		}	
		else {
			System.out.println(payload.message);
			send("", true);
		}
	}

	public void send(String message, boolean acked) throws Exception {
		if (acked) {
			host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatSegment(seqNum++, message, acked));
		}
		else {
			host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatSegment(seqNum, "", acked));
		}
	}


    private class MyTimer extends AbstractTimer {
    	public MyTimer(AbstractScheduler scheduler, double interval) {
    		super(scheduler, interval, false);
    	}
    	protected void run() throws Exception {
			// TODO : Mettre ici le code a run une fois le timer expiré
		}
    }
    
    public void start()
    throws Exception {
    	//timer.start();
    }
    
    public void stop() {
    	timer.stop();
    }
}
