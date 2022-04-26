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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListenerList<T> {

    private final List<T> listeners= new ArrayList<T>();

    public void addListener(int index, T l) {
    	listeners.add(0, l);
    }
    
    public void addListener(T l) {
    	listeners.add(l);
    }
    
    public void removeListener(T l) {
    	listeners.remove(l);
    }
    
    public Collection<T> getListeners() {
    	return listeners;
    }
	
}
