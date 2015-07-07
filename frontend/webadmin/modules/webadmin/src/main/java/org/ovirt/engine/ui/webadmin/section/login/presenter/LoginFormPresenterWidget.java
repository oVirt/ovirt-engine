package org.ovirt.engine.ui.webadmin.section.login.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractLoginPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class LoginFormPresenterWidget extends AbstractLoginPresenterWidget<LoginModel, LoginFormPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractLoginPresenterWidget.ViewDef<LoginModel> {
    }

    @Inject
    public LoginFormPresenterWidget(EventBus eventBus, ViewDef view, LoginModel loginModel,
            ClientStorage clientStorage, LockInteractionManager lockInteractionManager) {
        super(eventBus, view, loginModel, clientStorage, lockInteractionManager);
    }

    @Override
    protected String getSelectedDomainKey() {
        return "Login_SelectedDomain_WebAdmin"; //$NON-NLS-1$
    }

}
