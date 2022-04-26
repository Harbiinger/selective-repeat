package reso.examples.queueing;

public class PacketGenRandomExponential
extends AbstractPacketGenerator {

	/** Generates packet interval according to an exponential distribution.
	 * @param pktRate is the mean packet rate. A mean packet rate R (e.g. 10 pkts/s)
	 * will lead to a mean inter-packet interval of 1/R (e.g. 0.1 s) 
	 * 
	 * This packet generator should lead to packet counts per unit of time that are
	 * Poisson distributed.
	 */
	public PacketGenRandomExponential(double pktRate, int numPkts) {
		super(pktRate, numPkts);
	}

	@Override
	public double nextPacketInterval() {
		return getExponential(pktRate);
	}
	
	/**
	 * Generate random numbers that are exponentially distributed.
	 * 
	 * @param lambda is the exponential distribution's parameter.
	 * @return a random value
	 */
	private static double getExponential(double lambda) {
		double p= Math.random();
		return -Math.log(1-p)/lambda;
	}
		
}
