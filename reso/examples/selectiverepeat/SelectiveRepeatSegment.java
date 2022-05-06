package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatSegment implements Message {
	
	public final String message; 
	
	public SelectiveRepeatSegment(String message) {
        this.message = message;
    }
	
	public String toString() {
		return "Selective Repeat [message=" + message + "]";
	}

	@Override
	public int getByteLength() {
		// TODO: do
		return 3;
	}

}
