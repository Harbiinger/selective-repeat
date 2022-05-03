package reso.testing;

import org.junit.Test;

import reso.ip.IPAddress;
import junit.framework.TestCase;

public class TestIPAddress extends TestCase {
	
	@Test
	public void testGetByAddress() throws Exception {
		IPAddress a1= IPAddress.getByAddress(192, 168, 1, 2);
		assertEquals(a1.addr[0], (byte) (192-128));
		assertEquals(a1.addr[1], (byte) (168-128));
		assertEquals(a1.addr[2], (byte) (1-128));
		assertEquals(a1.addr[3], (byte) (2-128));
	}

	@Test
	public void testEquality() throws Exception {
		IPAddress a1= IPAddress.getByAddress("10.0.0.1");
		IPAddress a2= IPAddress.getByAddress("10.0.0.1");
		assertEquals(a1, a2);
	}
	
	@Test
	public void testComparison() throws Exception {
		IPAddress a1= IPAddress.getByAddress("10.0.0.1");
		IPAddress a2= IPAddress.getByAddress("10.0.0.2");
		IPAddress a3= IPAddress.ANY;
		IPAddress a4= IPAddress.BROADCAST;
		IPAddress a5= IPAddress.getByAddress(192, 168, 1, 2);
		IPAddress a6= IPAddress.getByAddress(192, 168, 255, 2);
		assertTrue(a1.compareTo(a1) == 0);
		assertTrue(a1.compareTo(a2) < 0);
		assertTrue(a2.compareTo(a1) > 0);
		assertTrue(a3.compareTo(a1) < 0);
		assertTrue(a4.compareTo(a1) > 0);
		assertTrue(a5.compareTo(a1) > 0);
		assertTrue(a1.compareTo(a5) < 0);
		assertTrue(a5.compareTo(a6) < 0);
	}
	
	@Test
	public void testBroadcast() throws Exception {
		IPAddress a1= IPAddress.BROADCAST;
		assertTrue(a1.isBroadcast());
		assertFalse(a1.isUndefined());
	}
	
	@Test
	public void testUndefined() throws Exception {
		assertTrue(IPAddress.ANY.isUndefined());
	}
	
	@Test
	public void testToString() throws Exception {
		assertEquals(IPAddress.ANY.toString(), "0.0.0.0");
		assertEquals(IPAddress.BROADCAST.toString(), "255.255.255.255");
	}
	
}
