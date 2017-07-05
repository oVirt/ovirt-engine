package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.auth.CommonCurrentUserRole;
import org.ovirt.engine.ui.common.gin.BaseSystemModule;
import org.ovirt.engine.ui.common.section.DefaultMainSectionPlace;
import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.place.WebAdminPlaceManager;
import org.ovirt.engine.ui.webadmin.system.PostMessageDispatcher;

/**
 * GIN module containing WebAdmin infrastructure and configuration bindings.
 */
public class SystemModule extends BaseSystemModule {

    @SuppressWarnings("deprecation")
    @Override
    protected void configure() {
        requestStaticInjection(ClientGinjectorProvider.class);
        requestStaticInjection(AssetProvider.class);
        bindInfrastructure();
        bindConfiguration();
    }

    void bindInfrastructure() {
        bindCommonInfrastructure(WebAdminPlaceManager.class);
        bind(PostMessageDispatcher.class).asEagerSingleton();
        bindTypeAndImplAsSingleton(CurrentUserRole.class, CommonCurrentUserRole.class);
    }

    void bindConfiguration() {
        bindConstant().annotatedWith(DefaultMainSectionPlace.class)
                .to(WebAdminApplicationPlaces.DEFAULT_MAIN_SECTION_PLACE);

        bindResourceConfiguration(ApplicationConstants.class, ApplicationMessages.class,
                ApplicationResources.class, ApplicationTemplates.class, ApplicationDynamicMessages.class);
    }

}
