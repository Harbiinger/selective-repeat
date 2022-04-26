package reso.ip.arp;

import reso.ethernet.EthernetAddress;

/**
 * This class models an ARP entry.
 * An ARP entry is used to map an IP address to an Ethernet address.
 * It is associated with a state and a lifetime.
 */
public class ARPEntry {

	public static enum STATE {
		/** Entry is currently being resolved (request sent) */
		INCOMPLETE,
		/** Entry has been resolved */
		REACHABLE,
		/** Entry statically configured */
		PERMANENT,   
	};
	
	/** The default lifetime of an ARP entry. */
	public static final int DEFAULT_LIFETIME = 5;
	/** A special value that marks a lifetime as infinite (used for PERMANENT entries). */
	public static final int INFINITE_LIFETIME= -1;
	
	/** The Ethernet address for this entry. */
	protected EthernetAddress addr;
	/** The current state of this entry. */
	protected STATE state;
	/** The lifetime of this entry (in seconds). */
	protected int lifetime;
	
	/**
	 * Create an ARP entry.
	 */
	public ARPEntry() {
		addr= null;
		state= STATE.INCOMPLETE;
		lifetime= DEFAULT_LIFETIME;
	}
	
	/**
	 * Create a static ARP entry.
	 * The state of this entry is PERMANENT.
	 * 
	 * @param addr is the Ethernet address of this entry.
	 */
	public ARPEntry(EthernetAddress addr) {
		this.addr= addr;
		this.state= STATE.PERMANENT;
		lifetime= INFINITE_LIFETIME;
	}
	
	/**
	 * Get the Ethernet address of this entry.
	 * 
	 * @return the Ethernet address.
	 */
	public EthernetAddress getAddress() {
		return addr;
	}
	
	/**
	 * Get the state of this entry.
	 *  
	 * @return the state of this entry.
	 */
	public STATE getState() {
		return state;
	}
	
	/**
	 * Make this entry reachable and provide the corresponding Ethernet address.
	 * 
	 * @param addr is the Ethernet address that must be associated to this entry.
	 */
	public void reachable(EthernetAddress addr) {
		this.addr= addr;
		this.state= STATE.REACHABLE;
	}
	
	/**
	 * Returns a String representation of this ARP entry.
	 */
	public String toString() {
		return state + "\t" + addr + "\t" + lifetime;
	}
	
}
