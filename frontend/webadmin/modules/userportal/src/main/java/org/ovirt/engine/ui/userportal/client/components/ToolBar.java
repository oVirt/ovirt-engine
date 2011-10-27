package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.layout.HLayout;

public class ToolBar extends HLayout {
	public ToolBar() {
		setWidth100();
		setHeight(30);
	}
	
	public void addButton(ToolBarButton button) {
		if (getMembers().length > 0)
			addSeparator();
		
		addMember(button);
	}
	
	public void addSeparator() {
		Img separator = new Img("../skins/UserPortal/images/ToolStrip/separator.png");
		separator.setHeight(30);
		separator.setWidth(8);
		addMember(separator);
	}
}
