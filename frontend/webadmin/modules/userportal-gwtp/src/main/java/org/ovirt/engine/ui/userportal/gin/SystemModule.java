package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.ui.common.gin.BaseSystemModule;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.auth.CurrentUserRole;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.place.UserPortalPlaceManager;
import org.ovirt.engine.ui.userportal.section.DefaultMainSectionExtendedPlace;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemMessages;
import org.ovirt.engine.ui.userportal.system.ApplicationInit;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * GIN module containing UserPortal infrastructure and configuration bindings.
 */
public class SystemModule extends BaseSystemModule {

    @Override
    protected void configure() {
        bindInfrastructure();
        bindConfiguration();
    }

    void bindInfrastructure() {
        bindCommonInfrastructure();
        bind(PlaceManager.class).to(UserPortalPlaceManager.class).in(Singleton.class);
        bind(ApplicationInit.class).asEagerSingleton();
        bind(CurrentUserRole.class).in(Singleton.class);
    }

    void bindConfiguration() {
        bindPlaceConfiguration(ApplicationPlaces.DEFAULT_LOGIN_SECTION_PLACE,
                ApplicationPlaces.DEFAULT_MAIN_SECTION_BASIC_PLACE);
        bindConstant().annotatedWith(DefaultMainSectionExtendedPlace.class)
                .to(ApplicationPlaces.DEFAULT_MAIN_SECTION_EXTENDED_PLACE);
        bindResourceConfiguration(ApplicationConstants.class, ApplicationMessages.class,
                ApplicationResources.class, ApplicationTemplates.class);
        bind(ApplicationResourcesWithLookup.class).in(Singleton.class);
        bind(MainTabBasicListItemMessages.class).in(Singleton.class);
    }

}
