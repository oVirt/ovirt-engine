package org.ovirt.engine.ui.userportal.system;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.system.BaseApplicationInit;
import org.ovirt.engine.ui.common.uicommon.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.common.uicommon.FrontendFailureEventListener;
import org.ovirt.engine.ui.uicommonweb.ITypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalLoginModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ApplicationInit extends BaseApplicationInit {

    @Inject
    public ApplicationInit(ITypeResolver typeResolver,
            FrontendEventsHandlerImpl frontendEventsHandler,
            FrontendFailureEventListener frontendFailureEventListener,
            CurrentUser user,
            Provider<UserPortalLoginModel> loginModelProvider,
            EventBus eventBus) {
        super(typeResolver, frontendEventsHandler, frontendFailureEventListener, user, loginModelProvider, eventBus);
    }

    @Override
    protected void beforeUiCommonInitEvent(LoginModel loginModel) {
        UserPortalModelInitEvent.fire(eventBus);
    }

}
