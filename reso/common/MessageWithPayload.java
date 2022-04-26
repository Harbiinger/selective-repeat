package reso.common;

public interface MessageWithPayload extends Message {

	public Message getPayload();
	public int getProtocol();
	
}
