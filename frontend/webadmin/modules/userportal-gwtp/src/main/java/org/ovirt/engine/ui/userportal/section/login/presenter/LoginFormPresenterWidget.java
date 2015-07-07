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

    private final ConnectAutomaticallyProvider connectAutomaticallyProvider;

    @Inject
    public LoginFormPresenterWidget(EventBus eventBus, ViewDef view, UserPortalLoginModel loginModel,
            ClientStorage clientStorage, LockInteractionManager lockInteractionManager,
            ConnectAutomaticallyProvider connectAutomaticallyProvider) {
        super(eventBus, view, loginModel, clientStorage, lockInteractionManager);
        this.connectAutomaticallyProvider = connectAutomaticallyProvider;
    }

    @Override
    protected void onBind() {
        super.onBind();

        UserPortalLoginModel loginModel = getView().flush();
        initConnectAutomatically(loginModel);
    }

    @Override
    protected void onLoggedInEvent(UserPortalLoginModel loginModel) {
        super.onLoggedInEvent(loginModel);
        saveConnectAutomatically(loginModel);
    }

    void saveConnectAutomatically(UserPortalLoginModel loginModel) {
        Boolean isAutoconnect = loginModel.getIsAutoConnect().getEntity();
        connectAutomaticallyProvider.storeConnectAutomatically(isAutoconnect);
    }

    void initConnectAutomatically(UserPortalLoginModel loginModel) {
        boolean isAutoconnect = connectAutomaticallyProvider.readConnectAutomatically();
        loginModel.getIsAutoConnect().setEntity(isAutoconnect);
    }

    @Override
    protected String getSelectedDomainKey() {
        return "Login_SelectedDomain_UserPortal"; //$NON-NLS-1$
    }

}
