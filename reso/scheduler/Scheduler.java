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

import java.util.PriorityQueue;

/**
 * This class is the core of the simulator. It keeps an ordered list of events,
 * according to their scheduled time of occurence.
 */
public class Scheduler
extends AbstractScheduler {

    private PriorityQueue<AbstractEvent> pq;
    private final boolean stopOnError;

    /**
     * Creates a Scheduler that can optionally stop on error.
     * 
     * @param stopOnError defines if the simulator must stop when a scheduled
     *                    event generates an exception.
     */
    public Scheduler(boolean stopOnError) {
    	this.pq= new PriorityQueue<AbstractEvent>();
    	this.stopOnError= stopOnError;
    }
    
    /**
     * Creates a Scheduler. This version of the constructor generates a Scheduler
     * that will not stop when an event throws an exception.
     * 
     * @see #Scheduler(boolean)
     */
    public Scheduler() {
    	this(false);
    }

    /**
     * Schedule an event. It is forbidden to schedule an event before the current
     * scheduler's time. Scheduling an event in the past causes an exception to be thrown.
     * 
     * @param evt is the event to be scheduled.
     */
    public void schedule(AbstractEvent evt) {
    	numEventsScheduled++;
    	if (evt.getTime() < getCurrentTime())
    		throw new RuntimeException("Cannot schedule event in the past");
    	pq.offer(evt);
    }
    
    /**
     * Test if more events are scheduled.
     */
    public boolean hasMoreEvents() {
    	return (pq.size() > 0);
    }

    /**
     * Run the next event, that is
     * <ol>
     *   <li>pop the closest event in the future,</li>
     *   <li>advance the simulated time to that event's time of occurence, and</li>
     *   <li>run the event's callback.</li>
     * </ol>
     */
    public void runNextEvent() {
    	numEventsProcessed++;
    	AbstractEvent evt= pq.poll();
    	time= evt.getTime();
    	try {
    		evt.run();
    	} catch (Exception e) {
    		System.out.println("Scheduler " + String.format("%.6f", getCurrentTime()) + " - " + e.getMessage());
    		System.out.flush();
    		if (stopOnError)
    			throw new RuntimeException("Simulator stopped - " + e.getMessage());
    	}
    }
    
    /**
     * Print every event remaining in the queue.
     * This can be useful for debugging purposes.
     */
    public void dumpEvents() {
    	for (AbstractEvent e: pq)
    		System.out.println("queued [" + e + "]");
    }
    

}

