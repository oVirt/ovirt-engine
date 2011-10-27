package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.types.VerticalAlignment;

public class ToolBarButton extends Button {
	public ToolBarButton(String title) {
		super(title);
		setSrc("../skins/UserPortal/images/ToolStrip/button/button.png");
		setTitleStyle("stretchImgButton");
		setLayoutAlign(VerticalAlignment.CENTER);
		setLabelHPad(7);
	}

	boolean disabled = false;
	
	// Needed because a rendering problem which occurs in IE8 which causes the screen to black out when disabling (BZ #725996)
	@Override
	public void setDisabled(boolean disabled) {
		setAttribute("disabled", disabled, true);
		markForRedraw();
	}
}
