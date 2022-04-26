package reso.examples.queueing;

public interface PacketGenerator {
	
	boolean hasMorePackets();
	int nextPacket();
	double nextPacketInterval();
	
}
