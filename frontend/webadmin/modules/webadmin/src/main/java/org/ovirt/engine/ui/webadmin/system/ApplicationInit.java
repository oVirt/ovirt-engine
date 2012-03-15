package org.ovirt.engine.ui.webadmin.system;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.system.BaseApplicationInit;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.model.CommonModelManager;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ApplicationInit extends BaseApplicationInit<LoginModel> {

    @Inject
    public ApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user,
            Provider<LoginModel> loginModelProvider,
            EventBus eventBus) {
        super(typeResolver, frontendEventsHandler, frontendFailureEventListener, user, loginModelProvider, eventBus);
    }

    @Override
    protected void beforeUiCommonInitEvent(LoginModel loginModel) {
        CommonModelManager.init(eventBus, user, loginModel);
    }

    @Override
    public void onLogout() {
        // Logout operation happens within the CommonModel SignOut event handler
        CommonModelManager.instance().SignOut();
        AsyncDataProvider.clearCache();
    }

    @Override
    protected void onLogin(final LoginModel loginModel) {
        // Initialize reports
        ReportInit.getInstance().init();

        // Perform login only after reports have been initialized
        ReportInit.getInstance().getReportsInitEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                performLogin(loginModel);
            }
        });
    }

}
