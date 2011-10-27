package org.ovirt.engine.ui.userportal.client.components;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicommon.models.ListModel;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;

public class SubTabGridWithToolbar extends SubTabGrid {
	private ToolBar toolBar;

	private List<Object> selectedItemsIds = new ArrayList<Object>();
	
	public SubTabGridWithToolbar(final ListModel model) {
		super();

		grid.addSelectionChangedHandler(new SelectionChangedHandler() {
			@Override
			public void onSelectionChanged(SelectionEvent event) {
		
				if (grid.getSelection().length == 1) {
					model.setSelectedItem(grid.getSelectedRecord().getAttributeAsObject("entity"));
				}

				List<Object> selectedItems = new ArrayList<Object>();

				selectedItemsIds.clear();
				for (ListGridRecord lgr : grid.getSelection()) {
					selectedItems.add(lgr.getAttributeAsObject("entity"));
					selectedItemsIds.add(lgr.getAttributeAsObject("entityGuid"));
					GWT.log("Adding: " + lgr.getAttributeAsObject("entityGuid"));
				}
				
				model.setSelectedItems(selectedItems);
			}
		});
	}
	
	protected void addLayer() {
		toolBar = new ToolBar();
		addMember(toolBar);
		super.addLayer();
	}
	
	public ToolBar getToolbar() {
		return toolBar;
	}

	@Override
	public void setData(RecordList data) {
		if (selectedItemsIds.isEmpty() || data.getLength() == 0) {
			super.setData(data);
			return;
		}
		
		ArrayList<Record> recordsToSelect = new ArrayList<Record>();
		for (int i=0; i<data.getLength(); i++) {
			Record currRecord = data.get(i);
			if (selectedItemsIds.contains(currRecord.getAttributeAsObject("entityGuid")))
				recordsToSelect.add(currRecord);
		}
		
		GWT.log("Rows to select: ");
		for (Record r : recordsToSelect) {
			GWT.log("*** " + r.getAttribute("name"));
		}
		
		super.setData(data);
		grid.selectRecords(recordsToSelect.toArray(new Record[0]));
	}
	
}
