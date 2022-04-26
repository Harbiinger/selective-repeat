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

public interface Interface
{
	
    public String getName();
    public String getType();
    public int getIndex();
    public Node getNode();
    public void setIndex(int index);
    
    public void up();
    public void down();
    public boolean isActive();
        
    
    // --- ATTRIBUTES MANAGEMENT ---
    
    String STATE= "state";
    
    public Object getAttribute(String attr);
    public void addAttrListener(InterfaceAttrListener l);
    public void removeAttrListener(InterfaceAttrListener l);
    
}
