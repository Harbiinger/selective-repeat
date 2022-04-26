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

import java.util.ArrayList;
import java.util.List;

import reso.common.Message;
import reso.ip.IPAddress;
import reso.ip.IPInterfaceAdapter;

public class DVMessage
implements Message {
	
	public static class DV {
		
		public final IPAddress dst;
		public final int metric;
		public final IPInterfaceAdapter oif;
		
		public DV(IPAddress dst, int metric, IPInterfaceAdapter oif) {
			this.dst= dst;
			this.metric= metric;
			this.oif= oif;
		}
		
		public String toString() {
			return "DV[" + dst + "," + ((metric == Integer.MAX_VALUE)?"inf":metric) + "," + oif + "]";
		}
		
	}
	
	public final List<DV> dvs;
	
	public DVMessage() {
		dvs= new ArrayList<DV>();
	}
	
	public void addDV(IPAddress dst, int metric, IPInterfaceAdapter oif) {
		dvs.add(new DV(dst, metric, oif));
	}
	
	public String toString() {
		String s= "";
		for (DV dv: dvs)
			s+= " " + dv;
		return s;
	}

	@Override
	public int getByteLength() {
		return dvs.size() * (IPAddress.ANY.getByteLength());
	}

}
