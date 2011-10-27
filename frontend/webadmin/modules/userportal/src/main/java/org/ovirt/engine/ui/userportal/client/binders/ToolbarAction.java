package org.ovirt.engine.ui.userportal.client.binders;

import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommon.UICommand;
import com.smartgwt.client.widgets.events.ClickHandler;

public class ToolbarAction {

	private UICommand uiCommand;
	private ClickHandler onClickAction;
	
	public ToolbarAction(UICommand uiCommand, ClickHandler onClickAction) {
		this.uiCommand = uiCommand;
		this.onClickAction = onClickAction;
	}

	public UICommand getUiCommand() {
		return uiCommand;
	}

	public void setUiCommand(UICommand uiCommand) {
		this.uiCommand = uiCommand;
	}

	public ClickHandler getOnClickAction() {
		return onClickAction;
	}

	public void setOnClickAction(ClickHandler onClickAction) {
		this.onClickAction = onClickAction;
	}	
}