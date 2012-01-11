package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.LoggerImpl;
import org.ovirt.engine.ui.common.uicommon.UiCommonDefaultTypeResolver;
import org.ovirt.engine.ui.common.uicommon.model.CommonModelManager;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * GIN module containing common UiCommon integration bindings.
 */
public abstract class BaseUiCommonModule extends AbstractGinModule {

    protected void bindCommonIntegration() {
        bind(ITypeResolver.class).to(UiCommonDefaultTypeResolver.class).in(Singleton.class);
        bind(FrontendEventsHandlerImpl.class).in(Singleton.class);
        bind(FrontendFailureEventListener.class).in(Singleton.class);
        bind(ILogger.class).to(LoggerImpl.class).in(Singleton.class);
    }

    protected void bindConfiguratorIntegration(Class<? extends Configurator> configurator) {
        bind(Configurator.class).to(configurator).in(Singleton.class);
    }

    @Provides
    @Singleton
    public LoginModel getLoginModel(final EventBus eventBus, final CurrentUser user) {
        final LoginModel loginModel = new LoginModel();

        loginModel.getLoggedInEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                Frontend.setLoggedInUser(loginModel.getLoggedUser());
                CommonModelManager.init(eventBus, user, loginModel);

                user.onUserLogin(loginModel.getLoggedUser().getUserName());
                clearPassword(loginModel);
            }
        });

        return loginModel;
    }

    void clearPassword(LoginModel loginModel) {
        String password = (String) loginModel.getPassword().getEntity();

        if (password != null) {
            // Replace all password characters with whitespace
            password = password.replaceAll(".", " ");
        }

        loginModel.getPassword().setEntity(password);
    }

}
