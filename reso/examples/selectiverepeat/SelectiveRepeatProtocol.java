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
	private final int           segmentSize = 8; // TODO : should be renamed to messageSize
	private       IPAddress     dst;
	private       AbstractTimer timer;
	private       int           windowSize;
	private       int           sstresh;
	private		  int 	        dupAckNb;
	private       int           seqBase;    // sequence number of the fist packet in the window
	private       int           seqNum;     // current sequence number
	private       int           nextSeqNum; // next sequence number in the window ("cursor")

	private ArrayList<String>                 messagesList = new ArrayList<String>(); // data to be send
	private ArrayList<SelectiveRepeatSegment> window       = new ArrayList<SelectiveRepeatSegment>(); // sender window
	private ArrayList<SelectiveRepeatSegment> buffer       = new ArrayList<SelectiveRepeatSegment>(); // buffer for received messag
	private String                            data         = "";              // data ready to be delivered to the application layer


	public SelectiveRepeatProtocol(IPHost host) {
		this.host       = host;
		double interval = 5.0;

		windowSize      = 1;
		seqBase         = 0;
		seqNum          = 0;
		nextSeqNum      = 0;
		sstresh			= 200;
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
		pipeliningSend();
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		dst = datagram.src;
		SelectiveRepeatSegment payload = (SelectiveRepeatSegment) datagram.getPayload();
		windowSize = payload.windowSize;
		System.out.println("Window size : "+windowSize); // debug

		// sender receives an ack
		if (payload.acked) {
			Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "sender", "received ack [seqNum="+payload.seqNum+", windowSize="+windowSize+"]");
			for (SelectiveRepeatSegment segment : window) {
				if (segment.seqNum == payload.seqNum) {
					segment.acked = true; // the corresponding segment is marked as acked
				}	
			}

			// If we are in slow start/fast recovery
			if(windowSize < sstresh){
				windowSize = windowSize*2;

				// Make sure that the exponential growth don't go past sstresh
				if(windowSize > sstresh){
					windowSize = sstresh;
				}
			} else {
				windowSize++;
			}

			verifyWindow();   // remove acked segments
			pipeliningSend(); // try to send new segments
		}	

		// receiver receives a message
		else {
			Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "receiver", "received message [seqNum="+payload.seqNum+", windowSize="+windowSize+"]");

			if (payload.seqNum >= seqBase && payload.seqNum < seqBase+windowSize) {
				sendAck();
				addToBuffer(payload); // add segment to the buffer in order
				verifyBuffer();       // deliver data in order
			}

			if(dupAckNb == 3){
				windowSize = windowSize/2;
				dupAckNb = 0; 
			}

		}
	}

	/*
	 * Send packets until the window is full
	 */
	public void pipeliningSend() throws Exception {
		while (window.size() <= windowSize) {	
			SelectiveRepeatSegment segment = new SelectiveRepeatSegment(seqNum, messagesList.get(seqBase + nextSeqNum++), windowSize);
			send(segment);
		}
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "sender", "window is full");
	}

	/*
	 * Method to send one packet
	 */
	public void send(SelectiveRepeatSegment segment) throws Exception {
		window.add(segment);
		host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, segment);
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "sender", "sent segment [seqNum="+seqNum+", windowSize="+windowSize+"]");
		increaseSeqNum();
	}

	/*
	 * Method to send an ack to dst
	 */
	public void sendAck() throws Exception {
		host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatSegment(seqNum, true, windowSize));
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "receiver", "sent ack [seqNum="+seqNum+", windowSize="+windowSize+"]");
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
	}	
	// >>> by francois vion <<<

	/*

	 * This method increases the sequence number
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

			// Set the sstresh to the half of windows size and reset the window size to 1
			sstresh = windowSize/2;
			windowSize = 1;
						
		}
    }
    
    public void start() throws Exception {
    	//timer.start();
    }
    
    public void stop() {
    	timer.stop();
    }
}
