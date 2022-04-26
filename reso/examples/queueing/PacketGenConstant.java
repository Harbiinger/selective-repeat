package reso.examples.queueing;

public class PacketGenConstant
extends AbstractPacketGenerator {

	public final int burstLen;
	
	public PacketGenConstant(double pktRate, int burstLen, int numPkts) {
		super(pktRate, numPkts);
		this.burstLen= burstLen;
	}
	
	public double nextPacketInterval() {
		System.out.println("seqnum " + seqNum + " burstlen " + burstLen + " modulo " + (seqNum % burstLen));
		if ((seqNum % burstLen) == 0)
			return 1.0 / (pktRate / burstLen);
		else
			return 0;
	}

}
