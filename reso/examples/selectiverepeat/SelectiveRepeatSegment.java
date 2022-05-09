package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatSegment implements Message {
	
	public final int    seqNum;
	public final String message; 
	public final int    ACK;
	
	public SelectiveRepeatSegment(int seqNum, String message, int ACK) {
		this.seqNum  = seqNum;
        this.message = message;
		this.ACK     = ACK;
    }

	@Override
	public int getByteLength() {
		// TODO: do
		return 3;
	}

	@Override
	public String toString() {
		if (ACK==1) {
			return "Selective Repeat [ACK=" + ACK + "]";
		}
		return "Selective Repeat [message=" + message + "]";
	}

}
