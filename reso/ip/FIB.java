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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FIB {
		
	private final Map<IPAddress, IPRouteEntry> routes= 
		new HashMap<IPAddress, IPRouteEntry>();
	    
	public FIB() {
	}
	    
	public IPRouteEntry lookup(IPAddress dst)
	throws Exception {
		IPRouteEntry re= routes.get(dst);
		if (re == null)
			return null;
	    if (re.oif == null) {
	    	IPRouteEntry re2= lookup(re.gateway);
	    	if (re2 == null)
	    		throw new Exception("Gateway unreachable [" + re.gateway + "]");
	    	re= new IPRouteEntry(dst, re2.oif, re.gateway, re.type);
	    }
	    return re;
	}

	public void add(IPRouteEntry re) {
		routes.put(re.dst, re);
	}
	    
	public void remove(IPAddress dst) {
		routes.remove(dst);
	}
	    
	public Collection<IPRouteEntry> getEntries() {
	   	return routes.values();
	}
		
}
