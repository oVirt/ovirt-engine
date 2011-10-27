package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.SortArrow;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class SubTabGrid extends VLayout {
	protected HLayout horizontalLayout;
	protected ListGrid grid;
	
	public SubTabGrid() {
		setWidth100();
		setHeight100();
		addLayer();
	}	

	protected void addLayer() {
		horizontalLayout = new HLayout();
		
		grid = new ListGrid();
		grid.setBaseStyle("vmGridRowStyle");
		grid.setShowSortArrow(SortArrow.NONE);
		grid.setCanSort(false);
		grid.setShowHeaderContextMenu(false);
		grid.setLeaveScrollbarGap(false);

		horizontalLayout.addMember(grid);
		addMember(horizontalLayout);
	}
	
    public void setFields(ListGridField... fields) {
    	grid.setFields(fields);
    }
    
    public void setData(RecordList data) {
    	grid.setData(data);
    }
    
    public ListGrid getGrid() {
    	return grid;
    }
}
