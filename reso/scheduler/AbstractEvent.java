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
package reso.scheduler;

public abstract class AbstractEvent
    implements Comparable<AbstractEvent>
{

	/** Used to generate unique event indexes */
	private static long indexGenerator= 0;
	
	/** Event priority is defined based on (time, index)
	 *  to allow strict, total ordering */
    private final double time;
    private final long index;

    /**
     * Create a new simulation event.
     * @param time in seconds the event should occur.
     */
    public AbstractEvent(double time) {
    	this.time= time;
    	if (indexGenerator == Long.MAX_VALUE)
    		throw new RuntimeException("Too many events");
    	this.index= (indexGenerator++);
    }

    /**
     * @return the time in seconds this event is scheduled.
     */
    public final double getTime() {
    	return time;
    }

    /**
     * Compare two events based on their time of occurence.
     * 
     * @param evt is another event.
     */
    public final int compareTo(AbstractEvent evt) {
    	if (this.time < evt.time)
    		return -1;
    	else if (this.time > evt.time)
    		return 1;
    	if (this.index < evt.index)
    		return -1;
    	else if (this.index > evt.index)
    		return 1;
    	return 0;
    }

    /**
     * This is the method that is called when an event is processed by the scheduler.
     * 
     * @throws Exception
     */
    public abstract void run() throws Exception;

}
