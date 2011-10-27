package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;

public class ChangeableEdgeHLayout extends ChangeableEdgeLayout {

	public ChangeableEdgeHLayout(int width, int height, String edgeImagePrefix,	String edgeImageSuffix, int edgeSize, String id) {
		super(width, height, edgeImagePrefix, edgeImageSuffix, edgeSize, id);
	}

	@Override
	protected Layout getLayout() {
		return new HLayout();
	}
}
