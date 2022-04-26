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
package reso.ip;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reso.common.Address;

public class IPAddress
	implements Address, Comparable<IPAddress>
{
    
	public final byte [] addr;

    public static final IPAddress ANY      = new IPAddress(0, 0, 0, 0);
    public static final IPAddress BROADCAST= new IPAddress(255, 255, 255, 255);
    public static final IPAddress LOCALHOST= new IPAddress(127, 0, 0, 1);
    
    private IPAddress(int a, int b, int c, int d)     {
    	addr= new byte [] {(byte) (a-128), (byte) (b-128), (byte) (c-128), (byte) (d-128)};
    }
    
    public boolean isBroadcast() {
    	for (byte b: addr)
    		if (b != (byte) (0xff-128))
    			return false;
    	return true;
    }
    
    public boolean isUndefined() {
    	for (byte b: addr)
    		if (b != (byte) (0-128))
    			return false;
    	return true;
    }

    public static IPAddress getByAddress(int a, int b, int c, int d)
    	throws Exception {
    	if ((a < 0) || (a > 255) || (b < 0) || (b > 255) || (c < 0) || (c > 255) || (d < 0) || (d > 255))
    		throw new Exception("Invalid IP address [" + a + "." + b + "." + c + ".d" + d + "]");
    	return new IPAddress(a, b, c, d);
    }
    
    public static IPAddress getByAddress(String s) throws Exception {
    	Pattern pattern= Pattern.compile("([0-9]+).([0-9]+).([0-9]+).([0-9]+)");
    	Matcher m= pattern.matcher(s);
    	if (!m.matches())
    		throw new Exception("Invalid IP address [" + s + "]");
    	if (m.groupCount() != 4)
    		throw new Exception("Invalid IP address [" + s + "]");
    	int [] bytes= new int [4];
    	for (int i= 1; i < m.groupCount()+1; i++) {
    		bytes[i-1]= Integer.valueOf(m.group(i));
    		if ((bytes[i-1] < 0) || (bytes[i-1] > 255))
    			throw new Exception("Invalid IP address [" + s + "]");
    	}
    	return new IPAddress(bytes[0], bytes[1], bytes[2], bytes[3]);
    }
    
    public String toString() {
    	String s= "";
    	for (byte b: addr) {
    		if (s.length() > 0)
    			s+= ".";
   			s+= (((int) b) + 128);
    	}
    	return s;
    }

    public boolean equals(Object obj) {
    	if (!(obj instanceof IPAddress))
    		return false;
    	IPAddress addr= (IPAddress) obj;
    	for (int i= 0; i < 4; i++)
    		if (this.addr[i] != addr.addr[i])
    			return false;
    	return true;
    }
    
    public int compareTo(IPAddress addr) {
    	for (int i= 0; i < 4; i++)
    		if (this.addr[i] < addr.addr[i])
    			return -1;
    		else if (this.addr[i] > addr.addr[i])
    			return 1;
    	return 0;
    }
    
    public int hashCode() {
    	int hc= 0;
    	int i;
    	for (i= 0; i < addr.length; i++)
    		hc+= (((int) (addr[i]+128)) << (8*i));
    	return hc;
    }

	@Override
	public int getByteLength() {
		return 4;
	}
    
}
