package org.ovirt.engine.ui.userportal.client.binders.interfaces;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.grid.ListGridField;

public interface ListModelDualGridBinder extends ListModelWithActionsBinder {
	public ListGridField[] getAdditionalFields();

	public RecordList calcAdditionalRecords();
	
	// This is the property name of the propery changed event that is raised when the additional data is updated
	public String getPropertyName();
}
