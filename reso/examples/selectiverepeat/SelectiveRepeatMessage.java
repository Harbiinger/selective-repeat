package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatMessage
implements Message {
	
	public final String message; 
	
	public SelectiveRepeatMessage(String message) {
        this.message = message;
    }
	
	public String toString() {
		return "Selective Repeat [=" + message + "]";
	}

	@Override
	public int getByteLength() {
		// The selective repeat message carries a single 'int'
		return Integer.SIZE / 8;
	}

}