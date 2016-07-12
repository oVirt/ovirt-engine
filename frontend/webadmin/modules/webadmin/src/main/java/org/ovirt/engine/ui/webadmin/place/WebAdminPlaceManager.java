package org.ovirt.engine.ui.webadmin.place;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.place.ApplicationPlaceManager;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.section.DefaultMainSectionPlace;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

public class WebAdminPlaceManager extends ApplicationPlaceManager {

    private final PlaceRequest defaultMainSectionRequest;

    @Inject
    public WebAdminPlaceManager(EventBus eventBus,
            TokenFormatter tokenFormatter,
            CurrentUser user,
            ClientAgentType clientAgentType,
            @DefaultMainSectionPlace String defaultMainSectionPlace) {
        super(eventBus, tokenFormatter, user, clientAgentType);
        this.defaultMainSectionRequest = PlaceRequestFactory.get(defaultMainSectionPlace);
    }

    @Override
    protected PlaceRequest getDefaultMainSectionPlace() {
        return resolveMainSectionPlace(ApplicationModeHelper.getUiMode());
    }

    PlaceRequest resolveMainSectionPlace(ApplicationMode uiMode) {
        switch (uiMode) {
        case GlusterOnly:
            return PlaceRequestFactory.get(WebAdminApplicationPlaces.volumeMainTabPlace);
        case VirtOnly:
        case AllModes:
        default:
            return defaultMainSectionRequest;
        }
    }

}
