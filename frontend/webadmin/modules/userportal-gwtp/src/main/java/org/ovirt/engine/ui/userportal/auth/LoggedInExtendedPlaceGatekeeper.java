package org.ovirt.engine.ui.userportal.auth;

import org.ovirt.engine.ui.common.auth.CurrentUser;

import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;

public class LoggedInExtendedPlaceGatekeeper implements Gatekeeper {

    private final CurrentUser user;
    private final UserPortalCurrentUserRole userRole;

    @Inject
    public LoggedInExtendedPlaceGatekeeper(CurrentUser user, CurrentUserRole userRole) {
        this.user = user;
        this.userRole = (UserPortalCurrentUserRole) userRole;
    }

    @Override
    public boolean canReveal() {
        return user.isLoggedIn() && userRole.isExtendedUser();
    }

}
