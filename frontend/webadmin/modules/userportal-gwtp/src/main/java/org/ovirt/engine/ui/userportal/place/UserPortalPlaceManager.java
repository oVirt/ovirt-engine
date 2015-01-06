package org.ovirt.engine.ui.userportal.place;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.place.ApplicationPlaceManager;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.section.DefaultMainSectionPlace;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.userportal.auth.UserPortalCurrentUserRole;
import org.ovirt.engine.ui.userportal.section.DefaultMainSectionExtendedPlace;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

public class UserPortalPlaceManager extends ApplicationPlaceManager {

    private final UserPortalCurrentUserRole userRole;

    private final PlaceRequest defaultMainSectionRequest;
    private final PlaceRequest defaultMainSectionExtendedRequest;

    @Inject
    public UserPortalPlaceManager(EventBus eventBus,
            TokenFormatter tokenFormatter,
            CurrentUser user,
            CurrentUserRole userRole,
            ClientAgentType clientAgentType,
            @DefaultMainSectionPlace String defaultMainSectionPlace,
            @DefaultMainSectionExtendedPlace String defaultMainSectionExtendedPlace) {
        super(eventBus, tokenFormatter, user, clientAgentType);
        this.userRole = (UserPortalCurrentUserRole) userRole;
        this.defaultMainSectionRequest = PlaceRequestFactory.get(defaultMainSectionPlace);
        this.defaultMainSectionExtendedRequest = PlaceRequestFactory.get(defaultMainSectionExtendedPlace);
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
