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

import reso.scheduler.AbstractEvent;
import reso.scheduler.AbstractScheduler;

public abstract class AbstractTimer {

	private final double interval;
	private final boolean repeat;
	protected final AbstractScheduler scheduler;
	private boolean running= false;
	
	/**
	 * Create a new timer.
	 * 
	 * @param scheduler is the scheduler used to handle this timer's events.
	 * @param interval is the interval in seconds between two timer events.
	 * @param repeat specifies if the timer auto-repeats or if there is a single event.
	 */
	public AbstractTimer(AbstractScheduler scheduler, double interval, boolean repeat) {
		this.interval= interval;
		this.repeat= repeat;
		this.scheduler= scheduler;
	}
	
	/**
	 * Start the timer.
	 */
	public void start() {
		running= true;
		runOnce();
	}
	
	/**
	 * Stop the timer.
	 * 
	 * Note that if there are events scheduled for this timer, they will still
	 * be processed by the scheduler, but the action won't be executed by
	 * the timer, i.e. the run() method won't be called.
	 */
	public void stop() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	private void runOnce() {
		scheduler.schedule(new AbstractEvent(scheduler.getCurrentTime()+interval) {
			public void run() throws Exception {
				AbstractTimer.this.runTask();
			}
			public String toString() {
				return "Timer event[" + getTime() + "]";
			}
		});
	}
	
	private void runTask() throws Exception {
		if (!running)
			return;
		run();
		if (repeat)
			runOnce();
	}
		
	/**
	 * This method must be implemented by the user. This method will be called
	 * each time the timer expires.
	 */
	protected abstract void run() throws Exception;
		
}
