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

/**
 * The Message interface is only used to identify that a class is a message.
 * It is used in the child interface MessageWithPayload to support message
 * encapsulation.  
 */
public interface Message {

	int getByteLength();
	
}
