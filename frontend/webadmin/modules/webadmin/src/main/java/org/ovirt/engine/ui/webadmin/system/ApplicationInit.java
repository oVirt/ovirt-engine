package org.ovirt.engine.ui.webadmin.system;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.system.BaseApplicationInit;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.common.uicommon.model.CommonModelManager;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ApplicationInit extends BaseApplicationInit {

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
        CommonModelManager.instance().SignOut();
    }

}
