package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatSegment implements Message {
	
	public final int    seqNum;
	public final String message; 
	public final boolean acked;
	public final int windowSize;
	
	public SelectiveRepeatSegment(int seqNum, String message, boolean acked, int windowSize) {
		this.seqNum  = seqNum;
        this.message = message;
		this.acked     = acked;
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
			return "Selective Repeat [ACK=" + acked + "] ws:"+windowSize;
		}
		return "Selective Repeat [message=" + message + "] ws:"+windowSize;
	}

}
