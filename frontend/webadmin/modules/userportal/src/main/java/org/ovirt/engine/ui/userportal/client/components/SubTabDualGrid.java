package org.ovirt.engine.ui.userportal.client.components;

import org.ovirt.engine.ui.uicommon.models.ListModel;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGridField;

public class SubTabDualGrid extends SubTabGridWithToolbar {
	
	private SubTabGrid additionalGrid;
	
	public SubTabDualGrid(ListModel model) {
		super(model);

		grid.setSelectionType(SelectionStyle.SINGLE);
		grid.setBorder("1px solid gray");
		
		additionalGrid = new SubTabGrid();
		additionalGrid.getGrid().setEmptyMessage("");
		additionalGrid.setBorder("1px solid gray");
		additionalGrid.getGrid().setSelectionType(SelectionStyle.NONE);
		
		// Create a spacer to separate between the viewers
		Canvas spacer = new Canvas();
		spacer.setWidth(10);

		horizontalLayout.addMember(spacer);
		additionalGrid.setWidth("50%");
		horizontalLayout.addMember(additionalGrid);		
	}
	
    public void setAdditionalFields(ListGridField... fields) {
    	additionalGrid.setFields(fields);
    }
    
    public void setAdditionalData(RecordList data) {
    	additionalGrid.setData(data);
    }
}
