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

public abstract class AbstractHardwareInterface<M extends Message>
extends AbstractInterface
implements HardwareInterface<M>
{

	private Link<M> link;
	protected final ListenerList<MessageListener<M>> listeners=
		new ListenerList<MessageListener<M>>();

	
    public AbstractHardwareInterface(Node node, String name) {
    	super(node, name);
    }
    
    public boolean isConnected() {
    	return (link != null);
    }

    public boolean isConnectedTo(Link<M> link) {
    	return (this.link  == link);
    }
    
    public Link<M> getLink() {
    	return link;
    }
    
    /**
     * Create a point-to-point connection between two hardware interfaces.
     */
	public void connectTo(Link<M> link)
		throws Exception
	{
		if (!link.isConnectedTo(this))
			throw new Exception("Interface cannot be connected to this link");
		if (isConnected()) {
			if (isConnectedTo(link))
				throw new Exception("Interface " + getNode().name + "." + getName() + " already connected to this link");
			else
				throw new Exception("Interface " + getNode().name + "." + getName() + " already connected to another link");
		}
		this.link= link;
	}
	
	
    // --- MESSAGE LISTENERS MANAGEMENT ---
    
	public void addListener(int index, MessageListener<M> l) {
		listeners. addListener(index, l);
	}
    public void addListener(MessageListener<M> l) {
    	listeners.addListener(l);
    }
    public void removeListener(MessageListener<M> l) {
    	listeners.removeListener(l);
    }
	
	protected void toListeners(M m)
	throws Exception {
		for (MessageListener<M> l: listeners.getListeners())
			l.receive(this, m);
	}
    
}
