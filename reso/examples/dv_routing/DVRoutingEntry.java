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
package reso.examples.dv_routing;

import reso.ip.IPAddress;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPRouteEntry;

public class DVRoutingEntry
extends IPRouteEntry {
	
	public final DVMessage.DV dv;
	
	public DVRoutingEntry(IPAddress dst, IPInterfaceAdapter oif, DVMessage.DV dv) {
		super(dst, oif, DVRoutingProtocol.PROTOCOL_NAME);
		this.dv= dv;
	}
	
	public String toString() {
		String s= super.toString();
		s+= ", metric=" + dv.metric;
		return s; 
	}

}
