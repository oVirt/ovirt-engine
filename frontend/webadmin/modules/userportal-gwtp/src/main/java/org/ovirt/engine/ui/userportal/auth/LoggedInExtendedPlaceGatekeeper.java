package org.ovirt.engine.ui.userportal.auth;

import org.ovirt.engine.ui.common.auth.CurrentUser;

import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

public class LoggedInExtendedPlaceGatekeeper implements Gatekeeper {

    private final CurrentUser user;
    private final CurrentUserRole userRole;

    @Inject
    public LoggedInExtendedPlaceGatekeeper(CurrentUser user, CurrentUserRole userRole) {
        this.user = user;
        this.userRole = userRole;
    }

    @Override
    public boolean canReveal() {
        return user.isLoggedIn() && userRole.isExtendedUser();
    }

}
