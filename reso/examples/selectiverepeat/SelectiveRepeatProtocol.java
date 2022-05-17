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
import java.util.Random;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_SELECTIVE_REPEAT= Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");

	private final int       segmentSize = 8;
	private final IPHost    host;
	private       IPAddress dst;
	private       int       windowSize;
	private       int       sstresh;
	private		  int 	    dupAckNb;
	private       int       seqBase;    // sequence number of the fist packet in the window
	private       int       seqNum;     // current sequence number
	private       int       nextSeqNum; // next sequence number in the window ("cursor")

	private ArrayList<String>                 messagesList = new ArrayList<String>(); // data to be send
	private ArrayList<SelectiveRepeatSegment> window       = new ArrayList<SelectiveRepeatSegment>(); // sender window
	private ArrayList<SelectiveRepeatSegment> buffer       = new ArrayList<SelectiveRepeatSegment>(); // buffer for received messages
	private ArrayList<MyTimer>                timersList   = new ArrayList<MyTimer>(); 
	private String                            data         = "";              // data ready to be delivered to the application layer


	public SelectiveRepeatProtocol(IPHost host) {
		this.host       = host;
		double interval = 1.0;
		windowSize      = 1;
		seqBase         = 0;
		seqNum          = 0;
		nextSeqNum      = 0;
		sstresh			= 200;
	}

	/*
	 * Method used by an application to send a message to the given host.
	 * @param dst : the destination host IPAddress
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

		// simulating packet loss (10%) 
		Random rand = new Random();
		if (rand.nextInt(5) == 1) {
			System.out.println(payload.seqNum + " lost"); // debug
		} else{
			// sender receives an ack
			if (payload.acked) {

				Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "sender", "received ack [seqNum="+payload.seqNum+", windowSize="+windowSize+"]");

				for (SelectiveRepeatSegment segment : window) {
					if (segment.seqNum == payload.seqNum) {
						segment.acked = true; // the corresponding segment is marked as acked
						stop(payload.seqNum);        // stop the timer
					}	
				}


				if(verifyWindow()){ // remove acked segments

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

				}   

				pipeliningSend(); // try to send new segments
			}	

			// receiver receives a message
			else {
				windowSize = payload.windowSize;

				Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "receiver", "received message [seqNum="+payload.seqNum+", windowSize="+windowSize+"]");

				// if (payload.seqNum >= seqBase && payload.seqNum < seqBase+windowSize) {
				if(payload.seqNum == 91){
					System.out.println(data);
				}

				sendAck(payload.seqNum);
				if (payload.seqNum < seqBase+windowSize) {
					addToBuffer(payload); // add segment to the buffer in order
					verifyBuffer();       // deliver data in order
				}



				if(dupAckNb == 3){
					windowSize = windowSize/2;
					dupAckNb = 0; 
				}
			}
		}

	}

	/*
	 * Send packets until the window is full
	 */
	public void pipeliningSend() throws Exception {

		while (window.size() < windowSize) {	
			if(seqNum<messagesList.size()){
				nextSeqNum++;
				String message = messagesList.get(seqNum);
				SelectiveRepeatSegment segment = new SelectiveRepeatSegment(seqNum, message, windowSize);
				send(segment);
			} else{
				break;
			}
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
		Tools.plot(host.getNetwork().getScheduler().getCurrentTime()*1000, windowSize);
		increaseSeqNum();
		start(segment); // start a timer
	}

	public void reSend(SelectiveRepeatSegment segment) throws Exception {
		stop(segment.seqNum); // stop previous timer
		host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, segment);
		start(segment); // start a new timer
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "sender", "resent segment [seqNum="+segment.seqNum+", windowSize="+windowSize+"]");
		Tools.plot(host.getNetwork().getScheduler().getCurrentTime()*1000, windowSize);
	}	

	/*
	 * Method to send an ack to dst
	 */
	public void sendAck(int seqNum) throws Exception {
		host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatSegment(seqNum, true, windowSize));
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "receiver", "sent ack [seqNum="+seqNum+", windowSize="+windowSize+"]");
	}

	/*
	 * This method removes the x first segments from the sender window
	 * if they are acked.
	 */
	private boolean verifyWindow() {
		boolean forwarded = false;
		int size = window.size();
		int cpt = 0;

		for(int i = 0; i < size; i++){
			if (!window.get(i-cpt).acked) {
				break;
			}
			window.remove(0);
			cpt++;
			seqBase    += 1;
			nextSeqNum -= 1;
			forwarded = true;
		}
		return forwarded;
	}

	/*
	 * Deliver data to the application layer
	 * in the right order
	 */
	private void verifyBuffer() {
		int size = buffer.size();
		int cpt = 0;

		for(int i = 0; i < size; i++){
			SelectiveRepeatSegment segment = buffer.get(i-cpt);
			if(segment.seqNum == seqBase){
				data    += segment.message; // delivering data 
				seqBase += 1;               // move the receiver window
				buffer.remove(0);
				cpt++;
			}
		}
	}

	/*
	 * Add segment to the buffer in order (sorted by seqNum)
	 */
	private void addToBuffer(SelectiveRepeatSegment segment) {
		if (buffer.size() == 0) {
			buffer.add(segment);
		} else {
			if(buffer.get(buffer.size()-1).seqNum < segment.seqNum){
				buffer.add(segment);
			} else{
				int size = buffer.size();
				for(int i = 0; i<size+1; i++){
					if (buffer.get(i).seqNum > segment.seqNum ) {
						buffer.add(i, segment);
					}
				}
			}
		}
	}	

	/*
	 * This method increases the sequence number
	 * this is not very useful because we don't use cylic sequence numbers
	 */
	private void increaseSeqNum() {
		seqNum += 1;
	}

    private class MyTimer extends AbstractTimer {
		
		private SelectiveRepeatSegment segment;

    	public MyTimer(AbstractScheduler scheduler, double interval, SelectiveRepeatSegment segment) {
    		super(scheduler, interval, false);
			this.segment = segment;
    	}

		private int getSeqNum() {
			return segment.seqNum;
		}

    	protected void run() throws Exception {
			Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "sender", "time out [SeqNum="+segment.seqNum+"]");
			reSend(segment); // retransmission of the segment 

			// Set the sstresh to the half of windows size and reset the window size to 1
			if(windowSize == 1){
				sstresh = 1;
			} else{
				sstresh = windowSize/2;
			}
			windowSize = 1;
		}

		@Override
		public String toString() {
			return "timer for " + segment.seqNum + "\n";
		}

    }

	private void start(SelectiveRepeatSegment segment) {
		MyTimer timer = new MyTimer(host.getNetwork().getScheduler(), 0.1, segment);
		timersList.add(timer);
		timer.start();
	}

	private void stop(int seqNum) {
		for (MyTimer timer : timersList) {
			if (timer.getSeqNum() == seqNum) {
				timer.stop();
				timersList.remove(timer);
				return;
			}
		}
	}
    
}
