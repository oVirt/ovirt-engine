package org.ovirt.engine.ui.userportal.client.binders.interfaces;

import com.smartgwt.client.widgets.grid.ListGridField;

public interface ListModelBinder extends ModelToViewerBinder {
	public ListGridField[] getFields();
}
