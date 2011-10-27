package org.ovirt.engine.ui.userportal.client.binders.specific;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.configure.PermissionListModel;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.ToolbarAction;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelWithActionsBinder;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.modalpanels.AddPermissionsModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.ItemRemoveModalPanel;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class PermissionListModelBinder implements ListModelWithActionsBinder {

	PermissionListModel model;

	RendererType rendererType = RendererType.GridWithToolbar;
	
	public void setModel(Model model) {
		this.model = (PermissionListModel)model;
	}
	
	@Override
	public ListGridField[] getFields() {
		return fields;
	}

	@Override
	public RecordList calcRecords() {
		RecordList records = new RecordList();

		ArrayList<permissions> permissions_list = (ArrayList<permissions>)model.getItems();

		if (permissions_list != null) {
			for (permissions p : permissions_list) {
				ListGridRecord r = new ListGridRecord();
				r.setAttribute("user", p.getOwnerName());
				r.setAttribute("role", p.getRoleName());
				r.setAttribute("entity", p);
				r.setAttribute("entityGuid", p.getId());
				records.add(r);
			}
		}
		
		return records;
	}

	private static ListGridField[] fields = {
			new ListGridField("user", "User", 400),
			new ListGridField("role", "Role"),
	};

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}

	@Override
	public ToolbarAction[] getCommands(final GridController gridController) {
		ToolbarAction[] actions = new ToolbarAction[] {
				new ToolbarAction(model.getAddCommand(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						model.getAddCommand().Execute();
						AddPermissionsModalPanel panel = new AddPermissionsModalPanel(model);
						panel.draw();
					}
				}),
				new ToolbarAction(model.getRemoveCommand(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						model.getRemoveCommand().Execute();
						ItemRemoveModalPanel panel = new ItemRemoveModalPanel(model.getWindow().getTitle(), model.getWindow().getMessage(), model, new ObjectNameResolver() {
							@Override
							public String getItemName(Object o) {
								return "Role " + ((permissions)o).getRoleName() + " on User " + ((permissions)o).getOwnerName();
							}
						}, gridController);
						panel.draw();
					}
				})
		};
		return actions;
	}
}
