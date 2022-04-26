/*******************************************************************************
 * Copyright (c) 2011 Bruno Quoitin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Bruno Quoitin - initial API and implementation
 ******************************************************************************/
package reso.ethernet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EthernetAddress
{

	public static final EthernetAddress BROADCAST= getBroadcast();
	
    public final byte [] addr;

    private EthernetAddress(int a, int b, int c, int d, int e, int f) {
    	this.addr= new byte [] {(byte) a, (byte) b, (byte) c,
    			(byte) d, (byte) e, (byte) f};
    }
    
    public static EthernetAddress getByAddress(int a, int b, int c, int d, int e, int f)
    	throws Exception {
    	if ((a < 0) || (a > 255) || (b < 0) || (b > 255) || (c < 0) || (c > 255) ||
    			(d < 0) || (d > 255) || (e < 0) || (e > 255) || (f < 0) || (f > 255))
    		throw new Exception("");
    	return new EthernetAddress(a, b, c, d, e, f);
    }
    
    public static EthernetAddress getByAddress(String s)
      throws Exception {
    	Pattern pattern= Pattern.compile("([0-9A-Fa-f]{2}).([0-9A-Fa-f]{2}).([0-9A-Fa-f]{2}).([0-9A-Fa-f]{2}).([0-9A-Fa-f]{2}).([0-9A-Fa-f]{2})");
    	Matcher m= pattern.matcher(s);
    	if (!m.matches())
    		throw new Exception("Invalid Ethernet address [" + s + "]");
    	if (m.groupCount() != 6)
    		throw new Exception("Invalid Ethernet address [" + s + "]");
    	int [] bytes= new int [6];
    	for (int i= 1; i < m.groupCount()+1; i++) {
    		bytes[i-1]= Integer.parseInt(m.group(i), 16);
    		if ((bytes[i-1] < 0) || (bytes[i-1] > 255))
    			throw new Exception("Invalid IP address [" + s + "]");
    	}
    	return new EthernetAddress(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
    }

    private static EthernetAddress getBroadcast() {
    	return new EthernetAddress(0xff, 0xff, 0xff, 0xff, 0xff, 0xff);
    }

    public boolean isBroadcast() {
    	for (byte b: addr)
    		if (b != (byte) 0xff)
    			return false;
    	return true;
    }

    public String toString() {
    	String s= "";
    	for (byte b: addr) {
    		if (s.length() > 0)
    			s+= ":";
    		if (b < 0)
    			s+= Integer.toHexString(((int) b) + 256);
    		else
    			s+= Integer.toHexString(b);
    	}
    	return s;
    }
    
    public boolean equals(Object obj) {
    	if (!(obj instanceof EthernetAddress))
    		return false;
    	EthernetAddress addr= (EthernetAddress) obj;
    	for (int i= 0; i < this.addr.length; i++)
    		if (this.addr[i] != addr.addr[i])
    			return false;
    	return true;  	
    }

}
