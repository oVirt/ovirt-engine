package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.StretchImgButton;

public class Tab extends StretchImgButton {
	
	private Canvas canvas;
	
	private static final int CAP_SIZE = 4;
	private static final int MIN_WIDTH = 72;
	private static final int HEIGHT = 21;
	
	private boolean isHidden = false;
	
	public Tab() {
		setSrc("tabs/tab.png");
		setBaseStyle("tabTitle");
		setCapSize(CAP_SIZE);
		setHeight(HEIGHT);
		setLayoutAlign(VerticalAlignment.BOTTOM);
		setWidth(MIN_WIDTH + CAP_SIZE * 2);
		setOverflow(Overflow.VISIBLE);
		setShowDown(false);
	}
	
	public Canvas getPane() {
		return canvas;
	}

	public void setPane(Canvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public void hide() {
		isHidden = true;
		super.hide();
	}
	
	@Override
	public void show() {
		isHidden = false;
		super.show();
	}
	
	public boolean isHidden() {
		return isHidden;
	}
	
}
