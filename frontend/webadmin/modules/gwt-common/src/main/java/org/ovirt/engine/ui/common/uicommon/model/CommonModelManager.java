package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;

/**
 * Manages the {@link CommonModel} instance shared by all model providers.
 */
public class CommonModelManager {

    private static CommonModel commonModel;

    /**
     * Creates new {@link CommonModel} instance and sets up necessary event listeners.
     * <p>
     * Should be called right after successful user authentication, before redirecting the user to the main section.
     */
    public static void init(final EventBus eventBus, final CurrentUser user, final LoginModel loginModel,
            final FrontendFailureEventListener frontendFailureEventListener) {
        commonModel = CommonModel.newInstance();

        commonModel.getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                MainModelSelectionChangeEvent.fire(eventBus, commonModel.getSelectedItem());
            }
        });

        commonModel.getSignedOutEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                loginModel.resetAfterLogout();
                user.onUserLogout();

                // Clear CommonModel reference after the user signs out,
                // use deferred command to ensure the reference is cleared
                // only after all UiCommon-related processing is over
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        commonModel = null;
                    }
                });
            }
        });
    }

    /**
     * Returns the {@link CommonModel} instance if the user is currently logged in, {@code null} otherwise.
     */
    public static CommonModel instance() {
        return commonModel;
    }

}
