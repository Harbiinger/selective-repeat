package reso.ip;

import reso.common.AbstractApplication;

public class ICMPProtocol
extends AbstractApplication
implements IPInterfaceListener {
	
	public static final String PROTO_NAME= "ICMP";
	public static final int PROTO_NUM= Datagram.allocateProtocolNumber(PROTO_NAME);
	
	private final IPLayer ip;
	
	public ICMPProtocol(IPHost host) {
		super(host, PROTO_NAME);
		ip= host.getIPLayer();
	}
	
	@Override
	public void start() throws Exception {
		ip.addListener(PROTO_NUM, this);
	}
	
	@Override
	public void stop() {
		ip.removeListener(PROTO_NUM, this);
	}
	
	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram)
			throws Exception {
		ICMPMessage msg= (ICMPMessage) datagram.getPayload();
		switch (msg.type) {
		case ICMPMessage.TYPE_ECHO_REQUEST:
			ip.send(IPAddress.ANY, datagram.src, PROTO_NUM, new ICMPMessage(ICMPMessage.TYPE_ECHO_REPLY));
			break;
		}
	}
	
	public void sendRequest(IPAddress dst)
		throws Exception {
		ip.send(IPAddress.ANY, dst, PROTO_NUM, new ICMPMessage(ICMPMessage.TYPE_ECHO_REQUEST));
	}

}
