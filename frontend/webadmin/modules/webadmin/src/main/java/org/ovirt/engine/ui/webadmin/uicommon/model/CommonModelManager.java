package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.auth.CurrentUser;

import com.google.gwt.event.shared.EventBus;

/**
 * Manages the {@link CommonModel} instance shared by all model providers.
 * <p>
 * Triggers following events upon certain actions:
 * <ul>
 * <li>{@link CommonModelChangeEvent} when a new CommonModel instance is created
 * </ul>
 */
public class CommonModelManager {

    private static CommonModel commonModel;

    /**
     * Creates new {@link CommonModel} instance and sets up necessary event listeners.
     * <p>
     * Should be called right after successful user authentication, before redirecting the user to the main section.
     */
    public static void init(final EventBus eventBus, final CurrentUser user, final LoginModel loginModel) {
        commonModel = new CommonModel();

        commonModel.getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                MainModelSelectionChangeEvent.fire(eventBus, commonModel.getSelectedItem());
            }
        });

        commonModel.getSignedOutEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                // Clear CommonModel reference after the user signs out
                commonModel = null;

                loginModel.getUserName().setEntity(null);
                loginModel.getPassword().setEntity(null);
                loginModel.getPassword().setIsChangable(true);
                loginModel.getUserName().setIsChangable(true);
                loginModel.getDomain().setIsChangable(true);
                loginModel.getLoginCommand().setIsExecutionAllowed(true);

                user.onUserLogout();
            }
        });

        // Let others know that the CommonModel reference has changed
        CommonModelChangeEvent.fire(eventBus, commonModel);
    }

    /**
     * Returns the {@link CommonModel} instance if the user is currently logged in, {@code null} otherwise.
     */
    public static CommonModel instance() {
        return commonModel;
    }

}
