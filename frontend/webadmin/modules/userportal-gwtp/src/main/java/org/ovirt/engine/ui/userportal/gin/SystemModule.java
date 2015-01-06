package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.ui.common.gin.BaseSystemModule;
import org.ovirt.engine.ui.common.section.DefaultMainSectionPlace;
import org.ovirt.engine.ui.common.system.ClientStorageKeyPrefix;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.auth.LoggedInExtendedPlaceGatekeeper;
import org.ovirt.engine.ui.userportal.auth.UserPortalCurrentUserRole;
import org.ovirt.engine.ui.userportal.place.UserPortalPlaceManager;
import org.ovirt.engine.ui.userportal.section.DefaultMainSectionExtendedPlace;
import com.google.inject.Singleton;

/**
 * GIN module containing UserPortal infrastructure and configuration bindings.
 */
public class SystemModule extends BaseSystemModule {

    private static final String CLIENT_STORAGE_KEY_PREFIX = "ENGINE_UserPortal"; //$NON-NLS-1$

    @SuppressWarnings("deprecation")
    @Override
    protected void configure() {
        requestStaticInjection(ClientGinjectorProvider.class);
        requestStaticInjection(AssetProvider.class);
        bindInfrastructure();
        bindConfiguration();
    }

    void bindInfrastructure() {
        bindCommonInfrastructure(UserPortalPlaceManager.class);
        bind(LoggedInExtendedPlaceGatekeeper.class).in(Singleton.class);
        bindTypeAndImplAsSingleton(CurrentUserRole.class, UserPortalCurrentUserRole.class);
    }

    void bindConfiguration() {
        bindConstant().annotatedWith(DefaultMainSectionPlace.class)
                .to(UserPortalApplicationPlaces.DEFAULT_MAIN_SECTION_BASIC_PLACE);
        bindConstant().annotatedWith(DefaultMainSectionExtendedPlace.class)
                .to(UserPortalApplicationPlaces.DEFAULT_MAIN_SECTION_EXTENDED_PLACE);
        bindConstant().annotatedWith(ClientStorageKeyPrefix.class)
                .to(CLIENT_STORAGE_KEY_PREFIX);

        bindResourceConfiguration(ApplicationConstants.class, ApplicationMessages.class,
                ApplicationResources.class, ApplicationTemplates.class, ApplicationDynamicMessages.class);
        bind(ApplicationResourcesWithLookup.class).in(Singleton.class);
    }

}
