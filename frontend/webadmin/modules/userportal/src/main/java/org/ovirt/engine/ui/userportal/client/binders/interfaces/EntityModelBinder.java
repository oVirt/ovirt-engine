package org.ovirt.engine.ui.userportal.client.binders.interfaces;

import com.smartgwt.client.widgets.viewer.DetailViewerField;

public interface EntityModelBinder extends ModelToViewerBinder {

	public DetailViewerField[] getFields();
	
	public Integer[] getNumOfRowsInColumn();
}
