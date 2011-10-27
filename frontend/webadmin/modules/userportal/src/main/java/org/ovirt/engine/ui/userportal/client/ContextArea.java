package org.ovirt.engine.ui.userportal.client;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.userportal.client.views.basic.UserPortalBasicView;
import org.ovirt.engine.ui.userportal.client.views.extended.UserPortalExtendedView;
import com.smartgwt.client.types.Visibility;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;

public class ContextArea extends HLayout {

	private Canvas displayedView;
	Layout extendedViewWrapper;
	Layout basicViewWrapper;
	
	public ContextArea() {
		GWT.log("Initializing Context Area");

		UserPortalExtendedView extendedView = new UserPortalExtendedView();
	
		UserPortalBasicView basicView = new UserPortalBasicView();
		
		extendedViewWrapper = new Layout();
		extendedViewWrapper.setLayoutRightMargin(10);
		extendedViewWrapper.addMember(extendedView);
		extendedViewWrapper.setVisibility(Visibility.HIDDEN);		
		
		basicViewWrapper = new Layout();
		basicViewWrapper.setLayoutRightMargin(10);
		basicViewWrapper.setLayoutLeftMargin(10);
		basicViewWrapper.addMember(basicView);
		basicViewWrapper.setVisibility(Visibility.HIDDEN);

		addMember(extendedViewWrapper);
		addMember(basicViewWrapper);
		
		redrawContextAreaAccordingToMode();
		
		Masthead.getModeChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				redrawContextAreaAccordingToMode();
			}
		});
	}

	private void redrawContextAreaAccordingToMode() {
		switch (Masthead.getUserPortalMode()) {
		case BASIC:
			setDisplayedView(basicViewWrapper);
			break;
		case EXTENDED:
				setDisplayedView(extendedViewWrapper);
			break;
		}
	}
	
	private void setDisplayedView(Canvas view) {
		if (displayedView != null) {
			displayedView.hide();
			for (Canvas canvas : displayedView.getChildren()) canvas.hide();
		}
		displayedView = view;
		displayedView.show();
		for (Canvas canvas : displayedView.getChildren()) canvas.show(); 
	}
}
