package reso.utilities;

public class Delays {
	
	public static double transmission(long bitRate, int pktLen) {
		return ((double) pktLen * 8) / bitRate;
	}

	public static double propagation(double length, double speed) {
		return length / speed;
	}
	
}
