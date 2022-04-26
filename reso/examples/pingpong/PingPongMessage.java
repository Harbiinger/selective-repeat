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
package reso.examples.pingpong;

import reso.common.Message;

public class PingPongMessage
implements Message {
	
	public final int num; 
	
	public PingPongMessage(int num) {
		this.num= num;
	}
	
	public String toString() {
		return "PingPong [num=" + num + "]";
	}

	@Override
	public int getByteLength() {
		// The ping-pong message carries a single 'int'
		return Integer.SIZE / 8;
	}

}
