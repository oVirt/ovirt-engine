package org.ovirt.engine.ui.common.place;

import java.util.logging.Logger;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent.UserLoginChangeHandler;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.models.MainModelSelectionChangeEvent;
import org.ovirt.engine.ui.uicommonweb.models.MainModelSelectionChangeEvent.MainModelSelectionChangeHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.DOM;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

/**
 * Place manager that handles transitions between different places in the application.
 */
public abstract class ApplicationPlaceManager extends PlaceManagerImpl implements UserLoginChangeHandler, MainModelSelectionChangeHandler {

    private static final Logger logger = Logger.getLogger(ApplicationPlaceManager.class.getName());

    private final CurrentUser user;
    protected final ClientAgentType clientAgentType;

    private PlaceRequest autoLoginRequest;

    public ApplicationPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter,
            CurrentUser user, ClientAgentType clientAgentType) {
        super(eventBus, tokenFormatter);
        this.user = user;
        this.clientAgentType = clientAgentType;

        eventBus.addHandler(UserLoginChangeEvent.getType(), this);
        eventBus.addHandler(MainModelSelectionChangeEvent.getType(), this);
    }

    /**
     * Work around Firefox bug
     * https://bugzilla.mozilla.org/show_bug.cgi?id=519028
     * (change location.hash leads to location bar icon (favicon) disappearance).
     * Simply detach and re-attach the favicon link tag on hashchange.
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        super.onValueChange(event);
        if (clientAgentType.isFirefox()) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    Node favicon = DOM.getElementById("id-link-favicon");  //$NON-NLS-1$
                    if (favicon != null) {
                        Node parent = favicon.getParentNode();
                        favicon.removeFromParent();
                        parent.appendChild(favicon);
                    }
                }
            });
        }
    }

    @Override
    public void revealDefaultPlace() {
        revealPlace(getDefaultPlace());
    }

    protected PlaceRequest getDefaultPlace() {
        return getDefaultMainSectionPlace();
    }

    /**
     * Returns the currently valid default "main" section place.
     */
    protected abstract PlaceRequest getDefaultMainSectionPlace();

    @Override
    public void revealErrorPlace(String invalidHistoryToken) {
        logger.warning("Invalid place request - no presenter proxy mapped to '" //$NON-NLS-1$
                + invalidHistoryToken + "'"); //$NON-NLS-1$
        revealDefaultPlace();
    }

    @Override
    public void revealUnauthorizedPlace(String unauthorizedHistoryToken) {
        // Since auto login happens through deferred command,
        // the original place request might appear as unauthorized
        if (user.isAutoLogin()) {
            autoLoginRequest = getCurrentPlaceRequest();

            // Disable auto login for subsequent place requests
            user.setAutoLogin(false);
        } else {
            logger.warning("Unauthorized place request - the user is not allowed to access '" //$NON-NLS-1$
                    + unauthorizedHistoryToken + "'"); //$NON-NLS-1$
            revealDefaultPlace();
        }
    }

    @Override
    public void onUserLoginChange(UserLoginChangeEvent event) {
        // Disable auto login for subsequent place requests
        user.setAutoLogin(false);

        if (autoLoginRequest != null) {
            revealPlace(autoLoginRequest);

            // Once revealed, disable auto login for subsequent events
            autoLoginRequest = null;
        } else {
            revealDefaultPlace();
        }
    }

    @Override
    public void onMainModelSelectionChange(MainModelSelectionChangeEvent event) {
        String nameToken = event.getMainModel().getApplicationPlace();
        PlaceRequest placeRequest;

        if (nameToken != null && event.getMainModel().getIsAvailable()) {
            placeRequest = PlaceRequestFactory.get(nameToken);
        } else {
            placeRequest = getDefaultPlace();
        }

        revealPlace(placeRequest);
    }

}
