package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.widgets.IButton;

public class Button extends IButton {

	public Button() {
		super();
		setAutoFit(true);
	}

	public Button(String title) {
		super(title);
		setAutoFit(true);
	}
}