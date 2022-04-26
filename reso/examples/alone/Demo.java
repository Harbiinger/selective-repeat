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
package reso.examples.alone;

import reso.common.*;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;

public class Demo
{

	/* This example creates a single host with two applications:
	 * - application 1 displays a message after 5 seconds
	 * - application 2 displays a message after 10 seconds
	 * Both applications use a Timer (simulated) for this purpose.
	 */
    public static void main(String [] args)
    {
    	AbstractScheduler scheduler= new Scheduler();
    	try {
    		Network network= new Network(scheduler);
    		Host host= new Host("H");
    		network.addNode(host);
    		host.addApplication(new AppAlone(host, 5.0));
    		host.addApplication(new AppAlone(host, 10.0));
    		host.start();
    		scheduler.run();
    	} catch (Exception e) {
    		System.err.println(e.getMessage());
    		e.printStackTrace(System.err);
    	}
    }

}
