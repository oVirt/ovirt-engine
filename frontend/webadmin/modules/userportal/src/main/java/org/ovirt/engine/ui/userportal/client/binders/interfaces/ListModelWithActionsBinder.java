package org.ovirt.engine.ui.userportal.client.binders.interfaces;

import org.ovirt.engine.ui.userportal.client.binders.ToolbarAction;
import org.ovirt.engine.ui.userportal.client.components.GridController;

public interface ListModelWithActionsBinder extends ListModelBinder {
	
	// We pass the grid controller as a parameter given the assumption that many sub tab actions has an impact on the main grid data and thus need to refresh it
	public ToolbarAction[] getCommands(GridController gridController);
}
