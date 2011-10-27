package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.auth.CurrentUser;
import org.ovirt.engine.ui.webadmin.gin.uicommon.ClusterModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.DataCenterModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.EventModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.HostModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.PoolModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.StorageModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.TemplateModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.UserModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.VirtualMachineModule;
import org.ovirt.engine.ui.webadmin.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.webadmin.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.webadmin.uicommon.LoggerImpl;
import org.ovirt.engine.ui.webadmin.uicommon.UiCommonDefaultTypeResolver;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelManager;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * GIN module containing UiCommon model and integration bindings.
 */
public class UiCommonModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bindModels();
        bindIntegration();
    }

    void bindModels() {
        // All model providers are ultimately referenced via ModelBoundTabData
        // and are therefore created early, just like eager singletons
        // TODO review: should we bind model providers explicitly as eager singletons?
        install(new DataCenterModule());
        install(new StorageModule());
        install(new ClusterModule());
        install(new VirtualMachineModule());
        install(new HostModule());
        install(new PoolModule());
        install(new TemplateModule());
        install(new UserModule());
        install(new EventModule());

        // SystemTreeModel
        bind(SystemTreeModelProvider.class).asEagerSingleton();

        // BookmarkListModel
        bind(BookmarkModelProvider.class).asEagerSingleton();

        // TagListModel
        bind(TagModelProvider.class).asEagerSingleton();
    }

    void bindIntegration() {
        bind(ITypeResolver.class).to(UiCommonDefaultTypeResolver.class).in(Singleton.class);
        bind(ILogger.class).to(LoggerImpl.class).in(Singleton.class);
        bind(Configurator.class).in(Singleton.class);
        bind(FrontendEventsHandlerImpl.class).in(Singleton.class);
        bind(FrontendFailureEventListener.class).in(Singleton.class);
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
