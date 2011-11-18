package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.auth.CurrentUser;
import org.ovirt.engine.ui.webadmin.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaceManager;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.login.DefaultLoginSectionPlace;
import org.ovirt.engine.ui.webadmin.section.main.DefaultMainSectionPlace;
import org.ovirt.engine.ui.webadmin.system.ApplicationInit;
import org.ovirt.engine.ui.webadmin.system.AsyncCallFailureHandler;
import org.ovirt.engine.ui.webadmin.system.ErrorPopupManager;
import org.ovirt.engine.ui.webadmin.system.InternalConfiguration;
import org.ovirt.engine.ui.webadmin.uicommon.ClientAgentType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.RootPresenter;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

/**
 * GIN module containing general infrastructure and configuration bindings.
 */
public class SystemModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bindInfrastructure();
        bindConfiguration();
    }

    void bindInfrastructure() {
        bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
        bind(PlaceManager.class).to(ApplicationPlaceManager.class).in(Singleton.class);
        bind(TokenFormatter.class).to(ParameterTokenFormatter.class).in(Singleton.class);
        bind(RootPresenter.class).asEagerSingleton();
        bind(ApplicationInit.class).asEagerSingleton();
        bind(CurrentUser.class).in(Singleton.class);
        bind(LoggedInGatekeeper.class).in(Singleton.class);
        bind(ErrorPopupManager.class).in(Singleton.class);
        bind(AsyncCallFailureHandler.class).asEagerSingleton();
        bind(InternalConfiguration.class).asEagerSingleton();
        bind(ClientAgentType.class).in(Singleton.class);
    }

    void bindConfiguration() {
        bindConstant().annotatedWith(DefaultLoginSectionPlace.class).to(ApplicationPlaces.loginPlace);
        bindConstant().annotatedWith(DefaultMainSectionPlace.class).to(ApplicationPlaces.virtualMachineMainTabPlace);
        bind(ApplicationConstants.class).in(Singleton.class);
        bind(ApplicationMessages.class).in(Singleton.class);
        bind(ApplicationResources.class).in(Singleton.class);
        bind(ApplicationTemplates.class).in(Singleton.class);
    }

}
