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
package reso.common;

import java.util.HashMap;
import java.util.Map;

// -------------------------------------------------------------
// This class manages a set of protocol identifiers.
// -------------------------------------------------------------
public class ProtocolTable {

	private int nextProtocolNumber= 0;
	private Map<String,Integer> protocolsByName=
		new HashMap<String,Integer>();
	private Map<Integer,String> protocolsByNumber=
		new HashMap<Integer,String>();
    
	public int allocateProtocolNumber(String name) {
		while (protocolsByNumber.containsKey(nextProtocolNumber))
			nextProtocolNumber++;
		protocolsByName.put(name, nextProtocolNumber);
		protocolsByNumber.put(nextProtocolNumber, name);
		return nextProtocolNumber++;
	}
	
	public void registerProtocolNumber(String name, int num)
	throws Exception {
		if (protocolsByNumber.containsKey(num))
			throw new Exception("Protocol number [" + num + "] already defined");
		if (protocolsByName.containsKey(name))
			throw new Exception("Protocol [" + name + "] already defined");
		protocolsByName.put(name, num);
		protocolsByNumber.put(num, name);
	}
	
	public void checkAllocated(int num)
	throws Exception {
		if (!protocolsByNumber.containsKey(num))
			throw new Exception("Unallocated protocol number (" + num + ")");
	}
	
}
