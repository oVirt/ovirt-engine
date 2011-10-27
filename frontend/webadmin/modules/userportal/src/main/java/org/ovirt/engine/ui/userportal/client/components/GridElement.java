package org.ovirt.engine.ui.userportal.client.components;

public interface GridElement<T> {
	public void updateValues(T item);
	
	public void select();
	
	public void deselect();
	
	public void setItemId(Object id);
	
	public Object getItemId();
}
