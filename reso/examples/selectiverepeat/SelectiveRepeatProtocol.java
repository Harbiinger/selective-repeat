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
	private final int       packetLoss; // pourcentage of losing a packet
	private final IPHost    host;
	private final String    actor;
	private       IPAddress dst;
	private       int       windowSize;
	private       int       sstresh;
	private       double    RTO;
	private       double    R;
	private       double    SRTT;
	private       double    devRTT;
	private       double    timeSpent;
	private       int       seqBase;    // sequence number of the fist packet in the window
	private       int       seqNum;     // current sequence number
	private       int       nextSeqNum; // next sequence number in the window ("cursor")
	private       int[]     seqNumAcked;


	private ArrayList<String>                 messagesList = new ArrayList<String>();                 // data to be send
	private ArrayList<SelectiveRepeatSegment> window       = new ArrayList<SelectiveRepeatSegment>(); // sender window
	private ArrayList<SelectiveRepeatSegment> buffer       = new ArrayList<SelectiveRepeatSegment>(); // buffer for received messages
	private ArrayList<MyTimer>                timersList   = new ArrayList<MyTimer>(); 
	private String                            data         = "";                                      // data ready to be delivered to the application layer

	public SelectiveRepeatProtocol(IPHost host, String actor, int packetLoss) {
		this.host       = host;
		this.packetLoss = packetLoss;
		this.actor      = actor;
		RTO = 3.0;
		R = -1.0;
		timeSpent = 0;
		windowSize      = 1;
		seqBase         = 0;
		seqNum          = 0;
		nextSeqNum      = 0;
		sstresh			= 100;
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
		seqNumAcked = new int[messagesList.size()];
		pipeliningSend();
	}

	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		dst = datagram.src;
		SelectiveRepeatSegment payload = (SelectiveRepeatSegment) datagram.getPayload();

		// simulating packet loss (packetLoss %) 
		Random rand = new Random();
		if (rand.nextInt(100) < packetLoss) {
			Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "packet lost", "[seqNum="+payload.seqNum+"]");
		} 
		else {

			// sender receives an ack
			if (actor.equals("bob") && payload.acked) {
				Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, actor, "received ack [seqNum="+payload.seqNum+", windowSize="+windowSize+"]");

				
				if(seqNumAcked[payload.seqNum] == 0){
					seqNumAcked[payload.seqNum]++;
					for (SelectiveRepeatSegment segment : window) {
						if (segment.seqNum == payload.seqNum) {
							segment.acked = true; // the corresponding segment is marked as acked
							stop(payload.seqNum); // stop the timer
						}	
					}
	
					if (verifyWindow()) { // remove acked segments
	
						// If we are in slow start/fast recovery
						if(windowSize < sstresh){
							windowSize = windowSize*2;
	
							// Make sure that the exponential growth don't go past sstresh
							if(windowSize > sstresh){
								windowSize = sstresh;
							}
						} 
						else {
							windowSize++;
						}
					}
					// if(R == -1.0){ // First ACK
					// 	R = host.getNetwork().getScheduler().getCurrentTime();
					// 	timeSpent = R;
					// 	SRTT = R;
					// 	devRTT = R/2;
					// 	RTO = SRTT + 4*devRTT;
					// } else{
					// 	R = host.getNetwork().getScheduler().getCurrentTime() - timeSpent;
					// 	timeSpent = host.getNetwork().getScheduler().getCurrentTime();
					// 	calculateRTO();
					// }
					pipeliningSend(); // try to send new segments
				}
				// else{
				// 	seqNumAcked[payload.seqNum]++;
				// 	if(seqNumAcked[payload.seqNum]%3 == 0){
				// 		windowSize = windowSize/2;
				// 	}
				// }
				

				
			}	

			// receiver receives a message
			else if (actor.equals("alice") && !payload.acked) {
				windowSize = payload.windowSize;
				Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, actor, "received message [seqNum="+payload.seqNum+", windowSize="+windowSize+"]");
				sendAck(payload.seqNum);

				if (payload.seqNum < seqBase+windowSize) {
					addToBuffer(payload); // add segment to the buffer in order
					verifyBuffer();       // deliver data in order
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
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, actor, "window is full");
	}

	/*
	 * Method to send one packet
	 */
	public void send(SelectiveRepeatSegment segment) throws Exception {
		window.add(segment);
		host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, segment);
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, actor, "sent segment [seqNum="+seqNum+", windowSize="+windowSize+"]");
		Tools.plot(host.getNetwork().getScheduler().getCurrentTime()*1000, windowSize, RTO);
		increaseSeqNum();
		start(segment); // start a timer
	}

	public void reSend(SelectiveRepeatSegment segment) throws Exception {
		stop(segment.seqNum); // stop previous timer
		host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, segment);
		start(segment);       // start a new timer
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, actor, "resent segment [seqNum="+segment.seqNum+", windowSize="+windowSize+"]");
		Tools.plot(host.getNetwork().getScheduler().getCurrentTime()*1000, windowSize, RTO);
	}	

	/*
	 * Method to send an ack to dst
	 */
	public void sendAck(int seqNum) throws Exception {
		host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatSegment(seqNum, true, windowSize));
		Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, actor, "sent ack [seqNum="+seqNum+", windowSize="+windowSize+"]");
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

				// debug
				if(segment.seqNum == 92){
					System.out.println(data);
				}
			}
		}
	}

	/*
	 * Add segment to the buffer in order (sorted by seqNum)
	 */
	private void addToBuffer(SelectiveRepeatSegment segment) {
		int size = buffer.size();
		if (size == 0) {
			buffer.add(0, segment);
		} 
		else if (size == 1 && segment.seqNum != buffer.get(0).seqNum) {
			if (segment.seqNum < buffer.get(0).seqNum) {
				buffer.add(0, segment);
			}
			else {
				buffer.add(1, segment);
			}
		}
		else if (segment.seqNum < buffer.get(0).seqNum) {
			buffer.add(0, segment);
		}
		else if (segment.seqNum > buffer.get(size-1).seqNum) {
			buffer.add(segment);
		}
		else {
			for (int i = 1; i < size; i++) {
				if (segment.seqNum != buffer.get(i-1).seqNum && segment.seqNum < buffer.get(i).seqNum) {
					buffer.add(i, segment);
					return;
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

	private void calculateRTO(){
		SRTT = (1.0 - 0.125)*SRTT+ 0.125* R;
		devRTT = (1.0 - 0.25)*devRTT+ 0.25*(Math.abs(SRTT-R));
		RTO = SRTT + 4* devRTT;
	}

    private class MyTimer extends AbstractTimer {
		private SelectiveRepeatSegment segment;

    	public MyTimer(AbstractScheduler scheduler, double interval, SelectiveRepeatSegment segment) {
    		super(scheduler, interval, false);
			this.segment = segment;
    	}

		private int getSeqNum() {
			return this.segment.seqNum;
		}

    	protected void run() throws Exception {
			Tools.log(host.getNetwork().getScheduler().getCurrentTime()*1000, "sender", "time out [SeqNum="+segment.seqNum+"]");
			// RTO = RTO *2;
			reSend(this.segment); // retransmission of the segment 

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
		MyTimer timer = new MyTimer(host.getNetwork().getScheduler(), RTO, segment);
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