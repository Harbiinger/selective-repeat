package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatSegment implements Message {
	
	public final int     seqNum;
	public final String  message; 
	public       boolean acked;
	public final int     windowSize;
	
	public SelectiveRepeatSegment(int seqNum, boolean ack, int windowSize) {
		this.seqNum     = seqNum;
		acked           = ack;
		message         = null;
		this.windowSize = windowSize;
	}

	public SelectiveRepeatSegment(int seqNum, String message, int windowSize) {
		this.seqNum     = seqNum;
    	this.message    = message;
		acked           = false;
		this.windowSize = windowSize;
  }

	@Override
	public int getByteLength() {
		// TODO: do
		return 3;
	}

	@Override
	public String toString() {
		if (acked) {
			return "Selective Repeat [seqNum="+seqNum+", ack=1]";
		}
		return "Selective Repeat [seqNum="+seqNum+", message="+message+"]";
	}

}
