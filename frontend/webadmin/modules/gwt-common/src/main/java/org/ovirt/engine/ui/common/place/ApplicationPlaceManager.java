package org.ovirt.engine.ui.common.place;

import java.util.logging.Logger;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent.UserLoginChangeHandler;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

/**
 * Place manager that handles transitions between different places in the application.
 */
public abstract class ApplicationPlaceManager extends PlaceManagerImpl implements UserLoginChangeHandler {

    private static final Logger logger = Logger.getLogger(ApplicationPlaceManager.class.getName());

    private final CurrentUser user;
    private final PlaceRequest defaultLoginSectionRequest;

    private PlaceRequest autoLoginRequest;

    public ApplicationPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter,
            CurrentUser user, PlaceRequest defaultLoginSectionRequest) {
        super(eventBus, tokenFormatter);
        this.user = user;
        this.defaultLoginSectionRequest = defaultLoginSectionRequest;
        eventBus.addHandler(UserLoginChangeEvent.getType(), this);
    }

    @Override
    public void revealDefaultPlace() {
        revealPlace(getDefaultPlace());
    }

    protected PlaceRequest getDefaultPlace() {
        if (user.isLoggedIn()) {
            return getDefaultMainSectionPlace();
        } else {
            return defaultLoginSectionRequest;
        }
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

}
