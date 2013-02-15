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

    private final CurrentUserRole userRole;

    private final PlaceRequest defaultMainSectionRequest;
    private final PlaceRequest defaultMainSectionExtendedRequest;

    @Inject
    public UserPortalPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter,
            CurrentUser user, CurrentUserRole userRole,
            @DefaultLoginSectionPlace String defaultLoginSectionPlace,
            @DefaultMainSectionPlace String defaultMainSectionPlace,
            @DefaultMainSectionExtendedPlace String defaultMainSectionExtendedPlace) {
        super(eventBus, tokenFormatter, user, new PlaceRequest(defaultLoginSectionPlace));
        this.userRole = userRole;
        this.defaultMainSectionRequest = new PlaceRequest(defaultMainSectionPlace);
        this.defaultMainSectionExtendedRequest = new PlaceRequest(defaultMainSectionExtendedPlace);
    }

    @Override
    protected PlaceRequest getDefaultMainSectionPlace() {
        return userRole.isBasicUser() ? defaultMainSectionRequest : defaultMainSectionExtendedRequest;
    }

    public boolean isMainSectionBasicPlaceVisible() {
        return defaultMainSectionRequest.getNameToken().equals(
                getCurrentPlaceRequest().getNameToken());
    }

}
