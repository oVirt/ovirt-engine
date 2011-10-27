package org.ovirt.engine.ui.userportal.client.timers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.ui.uicommon.models.SearchableListModel;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.userportal.client.components.UserPortalTimerFactory;

public class SubTabRefreshTimer {
	private static Timer timer = UserPortalTimerFactory.factoryTimer("Sub Tab Refresh Timer", new Timer() {
		@Override
		public void run() {
			onTimerRun();
		}
	});

	private static SearchableListModel model;
	private static final int SUB_TAB_REFRESH_INTERVAL = 1000;

	private static void onTimerRun() {
		GWT.log("Sub tab refresh timer execution for the model: " + model.getClass().getName());
		if (model == null) {
			cancelTimer();
			return;
		}
	
		if (model.getSearchCommand().getIsExecutionAllowed()) {
			model.getSearchCommand().Execute();
		}
	}

	private static void cancelTimer() {
		GWT.log("Sub tab refresh timer cancelled");
		timer.cancel();
	}
	
	private static void startTimer() {
		timer.scheduleRepeating(SUB_TAB_REFRESH_INTERVAL);
	}
	
	public static void initSubTabRefreshTimer(SearchableListModel searchableListModel) {
		model = searchableListModel;
		model.getEntityChangedEvent().addListener(listener);
		startTimer();
	}

	private static IEventListener listener = new IEventListener() {
		@Override
		public void eventRaised(Event ev, Object sender, EventArgs args) {
			if (model != null) {
				model.getEntityChangedEvent().removeListener(listener);
				cancelTimer();
			}
		}
	};
}
