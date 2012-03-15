package org.ovirt.engine.ui.common.place;

import java.util.logging.Logger;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent.UserLoginChangeHandler;
import org.ovirt.engine.ui.common.section.DefaultLoginSectionPlace;
import org.ovirt.engine.ui.common.section.DefaultMainSectionPlace;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

/**
 * Place manager that handles transitions between different places in the application.
 */
public class ApplicationPlaceManager extends PlaceManagerImpl implements UserLoginChangeHandler {

    private static final Logger logger = Logger.getLogger(ApplicationPlaceManager.class.getName());

    protected final PlaceRequest defaultLoginSectionRequest;
    protected final PlaceRequest defaultMainSectionRequest;
    protected final CurrentUser user;

    private PlaceRequest autoLoginRequest;

    @Inject
    public ApplicationPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter,
            @DefaultLoginSectionPlace String defaultLoginSectionPlace,
            @DefaultMainSectionPlace String defaultMainSectionPlace,
            CurrentUser user) {
        super(eventBus, tokenFormatter);
        this.defaultLoginSectionRequest = new PlaceRequest(defaultLoginSectionPlace);
        this.defaultMainSectionRequest = new PlaceRequest(defaultMainSectionPlace);
        this.user = user;
        eventBus.addHandler(UserLoginChangeEvent.getType(), this);
    }

    @Override
    public void revealDefaultPlace() {
        revealPlace(getDefaultPlace());
    }

    protected PlaceRequest getDefaultPlace() {
        return user.isLoggedIn() ? defaultMainSectionRequest : defaultLoginSectionRequest;
    }

    @Override
    public void revealErrorPlace(String invalidHistoryToken) {
        logger.warning("Invalid place request - no presenter proxy mapped to '" + invalidHistoryToken + "'");
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
            logger.warning("Unauthorized place request - the user is not allowed to access '"
                    + unauthorizedHistoryToken + "'");
            revealDefaultPlace();
        }
    }

    @Override
    public void onUserLoginChange(UserLoginChangeEvent event) {
        if (autoLoginRequest != null) {
            revealPlace(autoLoginRequest);

            // Once revealed, disable auto login for subsequent events
            autoLoginRequest = null;
        } else {
            revealDefaultPlace();
        }
    }

}
