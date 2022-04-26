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

public abstract class AbstractMessage implements Message
{
	
    /*public static int length(ByteStreamable [] fields)
    {
	int len= 0;
	for (ByteStreamable bs: fields)
	    len+= bs.getBytesLength();
	return len;
    }*/

    /*public static byte [] concatenate(ByteStreamable [] fields)
    {
	byte [] bytes= new byte [length(fields)];
	int offset= 0;
	for (ByteStreamable bs: fields) {
	    int len= bs.getBytesLength();
	    System.arraycopy(bs.toBytes(), 0, bytes, offset, len);
	    offset+= len;
	}
	return bytes;
    }*/

}
