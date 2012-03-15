package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.common.section.DefaultLoginSectionPlace;
import org.ovirt.engine.ui.common.section.DefaultMainSectionPlace;
import org.ovirt.engine.ui.common.system.ApplicationFocusManager;
import org.ovirt.engine.ui.common.system.AsyncCallFailureHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.RootPresenter;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

/**
 * GIN module containing common infrastructure and configuration bindings.
 */
public abstract class BaseSystemModule extends AbstractGinModule {

    protected void bindCommonInfrastructure() {
        bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
        bind(TokenFormatter.class).to(ParameterTokenFormatter.class).in(Singleton.class);
        bind(RootPresenter.class).asEagerSingleton();
        bind(CurrentUser.class).in(Singleton.class);
        bind(LoggedInGatekeeper.class).in(Singleton.class);
        bind(ErrorPopupManager.class).in(Singleton.class);
        bind(AsyncCallFailureHandler.class).asEagerSingleton();
        bind(ClientAgentType.class).in(Singleton.class);
        bind(ClientStorage.class).in(Singleton.class);
        bind(ApplicationFocusManager.class).asEagerSingleton();
    }

    protected void bindPlaceConfiguration(String defaultLoginSectionPlace, String defaultMainSectionPlace) {
        bindConstant().annotatedWith(DefaultLoginSectionPlace.class).to(defaultLoginSectionPlace);
        bindConstant().annotatedWith(DefaultMainSectionPlace.class).to(defaultMainSectionPlace);
    }

    protected void bindResourceConfiguration(
            Class<? extends CommonApplicationConstants> constants,
            Class<? extends CommonApplicationMessages> messages,
            Class<? extends CommonApplicationResources> resources,
            Class<? extends CommonApplicationTemplates> templates) {
        bind(CommonApplicationConstants.class).to(constants).in(Singleton.class);
        bind(CommonApplicationMessages.class).to(messages).in(Singleton.class);
        bind(CommonApplicationResources.class).to(resources).in(Singleton.class);
        bind(CommonApplicationTemplates.class).to(templates).in(Singleton.class);
    }

}
