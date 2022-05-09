package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatSegment implements Message {
	
	public final int     seqNum;
	public final String  message; 
	public       boolean acked;
	
	public SelectiveRepeatSegment(int seqNum, boolean ack) {
		this.seqNum = seqNum;
		acked       = ack;
		message     = null;
	}

	public SelectiveRepeatSegment(int seqNum, String message) {
		this.seqNum  = seqNum;
        this.message = message;
		acked        = false;
    }

	@Override
	public int getByteLength() {
		// TODO: do
		return 3;
	}

	@Override
	public String toString() {
		if (acked) {
			return "Selective Repeat [ACK=" + acked + "]";
		}
		return "Selective Repeat [message=" + message + "]";
	}

}
