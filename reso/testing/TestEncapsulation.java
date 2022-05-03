package reso.testing;

import org.junit.Test;

import reso.common.AbstractMessage;
import reso.ethernet.EthernetAddress;
import reso.ethernet.EthernetFrame;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import static org.junit.Assert.*;

public class TestEncapsulation {

	public static final int DUMMY_PKT_LEN= 100;
	
	public static final int IP_PROTO_DUMMY= Datagram.allocateProtocolNumber("DUMMY");
	
	private class DummyMessage extends AbstractMessage {
		protected final int pktLen;
		public DummyMessage(int pktLen) {
			this.pktLen= pktLen;
		}
		public DummyMessage() {
			this(DUMMY_PKT_LEN);
		}
		public int getByteLength() {
			return pktLen;
		}	
	}
	
	@Test
	public void testIPDatagram()
		throws Exception
	{
		Datagram d= new Datagram(IPAddress.ANY, IPAddress.ANY, IP_PROTO_DUMMY, 255, new DummyMessage());
		assertEquals(20 + DUMMY_PKT_LEN, d.getByteLength());
	}
	
	@Test(expected=Exception.class)
	public void testIPDatagramOverflow()
		throws Exception
	{
		Datagram d= new Datagram(IPAddress.ANY, IPAddress.ANY, IP_PROTO_DUMMY, 255, new DummyMessage(65536));
	}
	
	@Test
	public void testFrame() throws Exception
	{
		EthernetFrame f= new EthernetFrame(EthernetAddress.BROADCAST, EthernetAddress.BROADCAST, EthernetFrame.PROTO.IP,
				new DummyMessage());
		assertEquals(18 + DUMMY_PKT_LEN, f.getByteLength());
	}
	
	@Test(expected=Exception.class)
	public void testFrameOverflow() throws Exception
	{
		EthernetFrame f= new EthernetFrame(EthernetAddress.BROADCAST, EthernetAddress.BROADCAST, EthernetFrame.PROTO.IP,
				new DummyMessage(2000));
	}
	
}
