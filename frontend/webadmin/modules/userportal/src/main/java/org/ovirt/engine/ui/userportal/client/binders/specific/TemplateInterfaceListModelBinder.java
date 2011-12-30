package org.ovirt.engine.ui.userportal.client.binders.specific;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.ToolbarAction;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelWithActionsBinder;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.modalpanels.ItemRemoveModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.NewNICModalPanel;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class TemplateInterfaceListModelBinder  implements ListModelWithActionsBinder {

	TemplateInterfaceListModel model;

	RendererType rendererType = RendererType.GridWithToolbar;
	
	private static ListGridField[] fields = {
		new ListGridField("name", "Name"),
		new ListGridField("networkName", "Network Name"),
		new ListGridField("type", "Type", 120),
};
	
	public void setModel(Model model) {
		this.model = (TemplateInterfaceListModel)model;
	}

	@Override
	public RendererType getRendererType() {
		return rendererType;
	}
	
	@Override
	public ToolbarAction[] getCommands(final GridController gridController) {
		ToolbarAction[] actions = new ToolbarAction[] {
			new ToolbarAction(model.getNewCommand(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					model.getNewCommand().Execute();
					NewNICModalPanel panel = new NewNICModalPanel(model, (VmInterfaceModel)model.getWindow());
					panel.draw();
				}
			}),
			new ToolbarAction(model.getEditCommand(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					model.getEditCommand().Execute();
					NewNICModalPanel panel = new NewNICModalPanel(model, (VmInterfaceModel)model.getWindow());
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
							return ((VmNetworkInterface)o).getName();
						}
					}, gridController);
					panel.draw();
				}
			})
		};
				
		return actions;
	}

	@Override
	public ListGridField[] getFields() {
		return fields;
	}

	@Override
	public RecordList calcRecords() {
		RecordList records = new RecordList();

		ArrayList<VmNetworkInterface> interfaces = (ArrayList<VmNetworkInterface>)model.getItems();

		Translator translator = EnumTranslator.Create(VmInterfaceType.class);

		if (interfaces != null) {
			for (VmNetworkInterface intrface : interfaces) {
				ListGridRecord r = new ListGridRecord();
				r.setAttribute("name", intrface.getName());
				r.setAttribute("networkName", intrface.getNetworkName());
				r.setAttribute("type", translator.get(VmInterfaceType.forValue(intrface.getType())));
				r.setAttribute("entity", intrface);
				r.setAttribute("entityGuid", intrface.getId());
				records.add(r);
			}
		}
		return records;
	}
}
