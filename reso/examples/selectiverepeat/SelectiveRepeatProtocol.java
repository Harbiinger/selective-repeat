package reso.examples.selectiverepeat;

import reso.scheduler.AbstractScheduler;
import reso.common.AbstractTimer;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;
import reso.ip.IPLayer;

import java.util.ArrayList;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_SELECTIVE_REPEAT= Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

	private final IPHost        host;
	private final int           segmentSize = 8; // should be rename to messageSize
	private       IPAddress     dst;
	private       AbstractTimer timer;
	private       int           windowSize;
	private       int           seqBase;    // sequence number of the fist packet in the window
	private       int           seqNum;     // current sequence number
	private       int           nextSeqNum; // next sequence number in the window ("cursor")

	private ArrayList<String>                 messagesList = new ArrayList(); // data to be send
	private ArrayList<SelectiveRepeatSegment> window       = new ArrayList(); // sender window
	private ArrayList<SelectiveRepeatSegment> buffer       = new ArrayList(); // buffer for received messages
	private String                            data         = "";              // data ready to be delivered to the application layer


	public SelectiveRepeatProtocol(IPHost host) {
		this.host       = host;
		double interval = 5.0;
		windowSize      = 8;
		seqBase         = 0;
		seqNum          = 0;
		nextSeqNum      = 0;
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
		send();
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		dst = datagram.src;
		SelectiveRepeatSegment payload = (SelectiveRepeatSegment) datagram.getPayload();

		// sender receive an ack
		if (payload.acked) {
			Tools.log("sender", "received ack [seqNum="+payload.seqNum+"]");
			for (SelectiveRepeatSegment segment : window) {
				if (segment.seqNum == payload.seqNum) {
					segment.acked = true; // the corresponding segment is marked as acked
				}	
			}
			verifyWindow(); // remove acked segments
			send();         // try to send new segments
		}	

		// receiver receive a message
		else {
			Tools.log("receiver", "received message [seqNum="+payload.seqNum+"]");
			if (payload.seqNum >= seqBase && payload.seqNum < seqBase+windowSize) {
				sendAck();
				addToBuffer(payload); // add segment to the buffer in order
				verifyBuffer();       // deliver data in order
			}
		}
	}

	public void send() throws Exception {
		while (window.size() < windowSize) {	
			SelectiveRepeatSegment segment = new SelectiveRepeatSegment(seqNum, messagesList.get(seqBase + nextSeqNum++));
			window.add(segment);	
			host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, segment);
			Tools.log("sender", "sent segment [seqNum="+seqNum+"]");
			increaseSeqNum();
		}
		Tools.log("sender", "window is full");
	}

	/*
	 * Method to send an ack to dst
	 */
	public void sendAck() throws Exception {
		host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatSegment(seqNum, true));
		Tools.log("receiver", "sent ack [seqNum="+seqNum+"]");
		increaseSeqNum();
	}

	/*
	 * This method removes the x first segments from the sender window
	 * if they are acked.
	 */
	private void verifyWindow() {
		for (SelectiveRepeatSegment segment : window) {
			if (!segment.acked) {
				break;
			}
			window.remove(0);
			seqBase    += 1;
			nextSeqNum -= 1;
		}
	}

	/*
	 * Deliver data to the application layer
	 * in the right order
	 */
	private void verifyBuffer() {
		for (SelectiveRepeatSegment segment : buffer) {
			if (segment.seqNum == seqBase) {
				data    += segment.message; // delivering data 
				seqBase += 1;               // move the receiver window
				buffer.remove(segment);
				System.out.println(segment.message); //debug
			}
		}
	}

	// le pire algorithme du 21e siecle 
	// auteur : francois vion
	/*
	 * Add segment to the buffer in order (sorted by seqNum)
	 */
	private void addToBuffer(SelectiveRepeatSegment segment) {
		if (buffer.size() == 0) {
			buffer.add(segment);
		} else {
			int i = 0;
			for (SelectiveRepeatSegment s : buffer) {
				if (segment.seqNum < s.seqNum) {
					buffer.add(i, segment);
				}
				i++;
			}
		}
	// >>> by francois vion <<<
	}

	/*
	 * This method increase the sequence number
	 * this is not very useful because we don't use cylic sequence numbers
	 */
	private void increaseSeqNum() {
		seqNum += 1;
	}

    private class MyTimer extends AbstractTimer {
    	public MyTimer(AbstractScheduler scheduler, double interval) {
    		super(scheduler, interval, false);
    	}
    	protected void run() throws Exception {
			// TODO : Mettre ici le code a run une fois le timer expiré
		}
    }
    
    public void start() throws Exception {
    	//timer.start();
    }
    
    public void stop() {
    	timer.stop();
    }

}
