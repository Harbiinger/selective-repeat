package reso.common;

public abstract class AbstractMessageWithPayload
implements MessageWithPayload {

	private final Message payload;
	private final int protocol;
	private final int headerLen;
	private final int payloadLen; /* This is a cache to avoid re-computing the length recursively */
	
	public AbstractMessageWithPayload(int headerLen, int maxPayloadLen, int protocol, Message payload)
		throws Exception {
		this.headerLen= headerLen;
		this.protocol= protocol;
		this.payload= payload;
		this.payloadLen= payload.getByteLength();
    	if (payloadLen > maxPayloadLen)
    		throw new Exception("payload length (" + payload.getByteLength() + ") larger than maximum (" + maxPayloadLen + ")");
	}
	
	@Override
	public Message getPayload() {
		return payload;
	}

	@Override
	public int getProtocol() {
		return protocol;
	}
	
	public int getHeaderLength() {
		return headerLen;
	}
	
	public int getByteLength() {
		return headerLen + payloadLen;
	}

}
