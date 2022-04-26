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

import java.util.ArrayList;
import java.util.List;

import reso.common.AbstractHardwareInterface;
import reso.common.Node;
import reso.scheduler.AbstractScheduler;

public class Hub
extends Node
{
	
	public final String name;
	public List<EthernetPort> ports;
	
	private class EthernetPort
	extends AbstractHardwareInterface<EthernetFrame> {

		private final Hub hub;
		
		public EthernetPort(Hub hub)
		throws Exception {
			super(hub, "port");
			this.hub= hub;
		}
		
		@Override
		public void receive(EthernetFrame m) {
			//hub.process(-1, m, this);
		}

		@Override
		public void send(EthernetFrame m) throws Exception {
			getLink().send(this, m);
		}
		
	}
	
	public Hub(String name, AbstractScheduler scheduler, int numPorts)
	throws Exception {
		super(name);
		this.name= name;
		ports= new ArrayList<EthernetPort>();
		for (int i= 0; i < numPorts; i++)
			ports.add(new EthernetPort(this));
	}

}
