package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

public class ChangeableEdgeVLayout extends ChangeableEdgeLayout {

	public ChangeableEdgeVLayout(int width, int height, String edgeImagePrefix, String edgeImageSuffix, int edgeSize, String id) {
        super(width, height, edgeImagePrefix, edgeImageSuffix, edgeSize, id);
    }

	@Override
	protected Layout getLayout() {
		return new VLayout();
	}
}
