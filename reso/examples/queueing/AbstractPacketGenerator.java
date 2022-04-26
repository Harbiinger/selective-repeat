package reso.examples.queueing;


public abstract class AbstractPacketGenerator
implements PacketGenerator {
	
	public final double pktRate; // Number of packets to generate per second
	protected int seqNum;        // Sequence number of next packet
	protected int numPkts;       // Number of packets to generate
	
	/**
	 * Constructor for a packet generator.
	 * 
	 * @param pktRate is the number of packets generated per second.
	 * @param numPkts is the number of packets to generate.
	 * 
	 * Note that if @param numPkts < 0, an infinite number of packets is generated
	 */
	AbstractPacketGenerator(double pktRate, int numPkts) {
		this.pktRate= pktRate;
		this.numPkts= numPkts;
		this.seqNum= 0;
	}
	
	/**
	 * Test if more packets can be generated.
	 * @return true if more packets must be generated and false otherwise.
	 * 
	 * This occurs if either
	 *   {@link numPkts} < 0 (infinite number of packets)
	 * or
	 *   {@link numPkts} > 0 (limit not reached).
	 */
	public boolean hasMorePackets() {
		return (numPkts < 0) || (numPkts > 0);
	}
	
	/**
	 * Generate the next packet's sequence number.
	 * This method increases the internal sequence number {@link seqNum} and
	 * decreases the internal packet counter {@link numPkts}.
	 */
	public int nextPacket() {
		if (numPkts > 0)
			numPkts--;
		return seqNum++;
	}
}
