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

import java.util.HashMap;

public abstract class AbstractInterface
implements Interface
{

	private final HashMap<String,Object> attrs=
		new HashMap<String,Object>();
	protected final ListenerList<InterfaceAttrListener> attrListeners=
		new ListenerList<InterfaceAttrListener>();
	
    private int index;
    private final String type;
    public final Node node;

    public AbstractInterface(Node node, String type) {
    	this.node= node;
    	this.type= type;
    	index= -1;
    	defineAttribute(STATE, Boolean.valueOf(true), false);
    }
    
    public void setIndex(int index) {
    	this.index= index;
    }
    
    public final String getName() {
    	return type+index;
    }
    
    public final String getType() {
    	return type;
    }

    public int getIndex() {
    	return index;
    }
    
    public Node getNode() {
    	return node;
    }
    
    public String toString() {
    	return getName();
    }
    
    public void up() {
    	setAttribute(STATE, Boolean.valueOf(true));
    }

    public void down() {
    	setAttribute(STATE, Boolean.valueOf(false));
    }
    
    public boolean isActive() {
    	return (Boolean) getAttribute(STATE);
    }
    
    
	// --- ATTRIBUTES MANAGEMENT ---
	
    /** Define a new attribute. An attribute value cannot be null.
     * The attribute listeners are normally not called when the attribute
     * is defined. To change that, use the advertise argument.  */
    protected void defineAttribute(String attr, Object value, boolean advertise) {
    	assert(!attrs.containsKey(attr));
    	attrs.put(attr, value);
    	if (advertise)
    		toAttrListeners(attr);
    }
    
    /** Change the value of an attribute. The attribute must exist. The attribute's value
     * cannot be null and its class must equal that of the previous value.
     * The attribute listeners are called when an attribute is changed.
     */
    protected void setAttribute(String attr, Object value) {
    	assert(attrs.containsKey(attr));
    	assert(attrs.get(attr).getClass() == value.getClass());
    	attrs.put(attr, value);
    	toAttrListeners(attr);
    }
    
    public Object getAttribute(String attr) {
    	return attrs.get(attr);
    }
	
	public void addAttrListener(InterfaceAttrListener l) {
		attrListeners.addListener(l);
	}

	public void removeAttrListener(InterfaceAttrListener l) {
		attrListeners.removeListener(l);
	}
	
	protected void toAttrListeners(String attr) {
		for (InterfaceAttrListener l: attrListeners.getListeners())
			l.attrChanged(this, attr);
	}


}
