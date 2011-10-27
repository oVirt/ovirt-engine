package org.ovirt.engine.ui.userportal.client;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.ui.userportal.client.util.messages.MessageCenterView;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;

public class Footer extends HLayout {
	private static final int FOOTER_HEIGHT = 40;
	private static final String FOOTER_BG_COLOR = "#FFFFFF";

	Label label;
	MessageCenterView recentMessage;

	public Footer() {
		super();
		// initialise the layout container
		setHeight(FOOTER_HEIGHT);
		setBackgroundColor(FOOTER_BG_COLOR);
		setAlign(Alignment.LEFT);

		// initialise the masthead label
		label = new Label();
		label.setAlign(Alignment.LEFT);
		label.setOverflow(Overflow.HIDDEN);
		label.setStyleName("engine-footer-text");
		// add the label to the layout container
		// addMember(label);

		recentMessage = new MessageCenterView();
		recentMessage.setWidth("*");
		if (!GWT.isScript()) {
			addMember(recentMessage);
		}
	}
}