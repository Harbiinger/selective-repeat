package reso.ip;

import reso.common.Message;

public class ICMPMessage
implements Message {
	
	public static final int TYPE_ECHO_REQUEST= 0;
	public static final int TYPE_ECHO_REPLY  = 1;
	
	public final int type;
	
	public ICMPMessage(int type) {
		this.type= type;
	}

	@Override
	public int getByteLength() {
		// Type (1), Code (1), Checksum (2)
		// Message Body (variable, at least IP header (20) + 8 bytes of payload)
		return 31;
	}
	
}