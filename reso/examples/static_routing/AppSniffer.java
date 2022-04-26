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
package reso.examples.static_routing;

import reso.common.AbstractApplication;
import reso.common.HardwareInterface;
import reso.common.Host;
import reso.common.Message;
import reso.common.MessageListener;

public class AppSniffer
extends AbstractApplication
implements MessageListener<Message> {
	
	private String [] ifNames;
	
	public AppSniffer(Host host, String [] ifNames) {
		super(host, "WireSnif");
		this.ifNames= ifNames;
	}
	
	public void receive(HardwareInterface<Message> iface, Message msg) {
		String time= String.format("%.6f", host.getNetwork().getScheduler().getCurrentTime());
		String ifaceName= host.name + "." + iface; 
		System.out.println(this.name + " " + time + " [" + ifaceName + "] [" + msg + "]");
	}
	
	public void start() {
		for (String ifName: ifNames) {
			HardwareInterface<Message> iface= (HardwareInterface<Message>) host.getInterfaceByName(ifName);
			iface.addListener(0, this);
		}
	}
	
	public void stop() {}

}
