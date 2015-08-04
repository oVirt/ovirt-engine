package org.ovirt.engine.ui.userportal.section.login.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractLoginPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalLoginModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class LoginFormPresenterWidget extends AbstractLoginPresenterWidget<UserPortalLoginModel, LoginFormPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractLoginPresenterWidget.ViewDef<UserPortalLoginModel> {
    }

    @Inject
    public LoginFormPresenterWidget(EventBus eventBus, ViewDef view, UserPortalLoginModel loginModel,
            ClientStorage clientStorage, LockInteractionManager lockInteractionManager) {
        super(eventBus, view, loginModel, clientStorage, lockInteractionManager);
    }

    @Override
    protected String getSelectedDomainKey() {
        return "Login_SelectedDomain_UserPortal"; //$NON-NLS-1$
    }

}
