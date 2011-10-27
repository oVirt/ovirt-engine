package org.ovirt.engine.ui.userportal.client.binders.specific;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommon.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.ToolbarAction;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelWithActionsBinder;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.modalpanels.ItemRemoveModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.NewNICModalPanel;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class VmInterfaceListModelBinder extends PoolInterfaceListModelBinder implements ListModelWithActionsBinder {

	VmInterfaceListModel model;

	RendererType rendererType = RendererType.GridWithToolbar;
	
	public void setModel(Model model) {
		super.setModel(model);
		this.model = (VmInterfaceListModel)model;
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
}
