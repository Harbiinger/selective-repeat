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

import reso.scheduler.*;

public class EventMessageSend<M extends Message>
	extends AbstractEvent
{

    public final M msg;
    public final HardwareInterface<M> src;
    public final MessageListener<M> dst;

    public EventMessageSend(double time, M msg, HardwareInterface<M> src, MessageListener<M> dst) {
    	super(time);
    	this.msg= msg;
    	this.src= src;
    	this.dst= dst;
    }

    public void run()
    throws Exception {
    	dst.receive(src, msg);
    }
    
}

