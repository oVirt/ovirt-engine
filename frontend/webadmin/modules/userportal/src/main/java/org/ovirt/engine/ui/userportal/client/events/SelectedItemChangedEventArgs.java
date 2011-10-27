package org.ovirt.engine.ui.userportal.client.events;

import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;

public class SelectedItemChangedEventArgs extends EventArgs {
	public Object selectedItem;
	
	public SelectedItemChangedEventArgs(Object selectedItem) {
		this.selectedItem = selectedItem; 
	}
}
