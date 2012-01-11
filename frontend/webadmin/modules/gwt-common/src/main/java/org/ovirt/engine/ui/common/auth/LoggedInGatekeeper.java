package org.ovirt.engine.ui.common.auth;

import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

/**
 * Protects access to application places where the user needs to be logged in.
 */
public class LoggedInGatekeeper implements Gatekeeper {

    private final CurrentUser user;

    @Inject
    public LoggedInGatekeeper(CurrentUser user) {
        this.user = user;
    }

    @Override
    public boolean canReveal() {
        return user.isLoggedIn();
    }

}
