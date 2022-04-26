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
import reso.scheduler.*;
import reso.common.AbstractTimer;

public class AppAlone extends AbstractApplication
{
	
    private AbstractTimer timer;

    public AppAlone(Host host, double interval) {
    	super(host, "alone");
    	timer= new MyTimer(host.getNetwork().getScheduler(), interval);
    }

    private class MyTimer extends AbstractTimer {
    	public MyTimer(AbstractScheduler scheduler, double interval) {
    		super(scheduler, interval, false);
    	}
    	protected void run() throws Exception {
			System.out.println("app=[" + name + "]" +
					" time=" + scheduler.getCurrentTime());
		}
    }
    
    public void start()
    throws Exception {
    	timer.start();
    }
    
    public void stop() {
    	timer.stop();
    }
}