package org.ovirt.engine.ui.userportal.client.binders.specific;

import java.util.ArrayList;

import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.vms.VmAppListModel;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.ToolbarAction;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelBinder;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class VmAppListModelBinder implements ListModelBinder {

	VmAppListModel model;

	RendererType rendererType = RendererType.Grid;

	@Override
	public RecordList calcRecords() {
		RecordList records = new RecordList();
		
		ArrayList<String> applications = (ArrayList<String>)model.getItems();				
		if (applications != null) {							
			for (String application : applications) {
				if (!application.isEmpty())
				{
					ListGridRecord r = new ListGridRecord();
					r.setAttribute("installedApplications", application);				
					records.add(r);
				}
			}			
		}
		return records;
	}

	@Override
	public void setModel(Model model) {
		this.model = (VmAppListModel)model;
	}

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}

	@Override
	public ListGridField[] getFields() {
		return fields;
	}
	
	private static ListGridField[] fields = {
		new ListGridField("installedApplications", "Installed Applications")
	};
	
}
