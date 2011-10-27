package org.ovirt.engine.ui.userportal.client.binders.specific;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.ui.uicommon.models.Model;
import org.ovirt.engine.ui.uicommon.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.binders.RendererType;
import org.ovirt.engine.ui.userportal.client.binders.ToolbarAction;
import org.ovirt.engine.ui.userportal.client.binders.interfaces.ListModelWithActionsBinder;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.modalpanels.ItemRemoveModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.NewDiskModalPanel;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class VmDiskListModelBinder extends PoolDiskListModelBinder implements ListModelWithActionsBinder {

	VmDiskListModel model;

	RendererType rendererType = RendererType.GridWithToolbar;
	
	@Override
	public void setModel(Model model) {
		super.setModel(model);
		this.model = (VmDiskListModel)model;
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
					NewDiskModalPanel panel = new NewDiskModalPanel(model, gridController);
					panel.draw();
				}
			}),
			new ToolbarAction(model.getEditCommand(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					model.getEditCommand().Execute();
					NewDiskModalPanel panel = new NewDiskModalPanel(model, gridController);
					panel.draw();
				}
			}),
			new ToolbarAction(model.getRemoveCommand(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					model.getRemoveCommand().Execute();
					ItemRemoveModalPanel p = new ItemRemoveModalPanel(model.getWindow().getTitle(), "Disk(s)", model, new ObjectNameResolver() {
						@Override
						public String getItemName(Object o) {
							return "Disk " + ((DiskImage)o).getinternal_drive_mapping();
						}
					}, gridController);

					p.draw();
				}
			})
		};
				
		return actions;
	}

}
