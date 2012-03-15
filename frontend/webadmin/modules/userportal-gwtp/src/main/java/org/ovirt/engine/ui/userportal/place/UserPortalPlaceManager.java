package org.ovirt.engine.ui.userportal.place;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.place.ApplicationPlaceManager;
import org.ovirt.engine.ui.common.section.DefaultLoginSectionPlace;
import org.ovirt.engine.ui.common.section.DefaultMainSectionPlace;
import org.ovirt.engine.ui.userportal.auth.CurrentUserRole;
import org.ovirt.engine.ui.userportal.section.DefaultMainSectionExtendedPlace;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

public class UserPortalPlaceManager extends ApplicationPlaceManager {

    private final PlaceRequest defaultMainSectionExtendedRequest;
    private final CurrentUserRole userRole;

    @Inject
    public UserPortalPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter,
            @DefaultLoginSectionPlace String defaultLoginSectionPlace,
            @DefaultMainSectionPlace String defaultMainSectionPlace,
            @DefaultMainSectionExtendedPlace String defaultMainSectionExtendedPlace,
            CurrentUser user, CurrentUserRole userRole) {
        super(eventBus, tokenFormatter, defaultLoginSectionPlace, defaultMainSectionPlace, user);
        this.defaultMainSectionExtendedRequest = new PlaceRequest(defaultMainSectionExtendedPlace);
        this.userRole = userRole;
    }

    @Override
    protected PlaceRequest getDefaultPlace() {
        if (user.isLoggedIn()) {
            return userRole.isBasicUser() ? defaultMainSectionRequest : defaultMainSectionExtendedRequest;
        } else {
            return defaultLoginSectionRequest;
        }
    }

}
