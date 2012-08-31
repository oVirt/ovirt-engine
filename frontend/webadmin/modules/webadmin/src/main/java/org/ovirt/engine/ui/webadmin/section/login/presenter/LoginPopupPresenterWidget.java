package org.ovirt.engine.ui.webadmin.section.login.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractLoginPopupPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.auth.SilentLoginData;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class LoginPopupPresenterWidget extends AbstractLoginPopupPresenterWidget<LoginModel, LoginPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractLoginPopupPresenterWidget.ViewDef<LoginModel> {
    }

    private SilentLoginData silentLoginData;

    @Inject
    public LoginPopupPresenterWidget(EventBus eventBus, ViewDef view, LoginModel loginModel,
            ClientStorage clientStorage, LockInteractionManager lockInteractionManager) {
        super(eventBus, view, loginModel, clientStorage, lockInteractionManager);
    }

    public void setSilentLoginData(SilentLoginData silentLoginData) {
        this.silentLoginData = silentLoginData;
    }

    @Override
    protected void onReset() {
        super.onReset();

        if (silentLoginData != null) {
            performSilentLogin();
        }
    }

    void performSilentLogin() {
        getView().clearErrorMessage();
        LoginModel loginModel = getView().flush();

        loginModel.getUserName().setEntity(silentLoginData.getUserName());
        loginModel.getPassword().setEntity(silentLoginData.getPassword());
        loginModel.getDomain().setSelectedItem(silentLoginData.getDomain());

        silentLoginData.setPassword(null);
        loginModel.Login();
    }

    @Override
    protected String getSelectedDomainKey() {
        return "Login_SelectedDomain_WebAdmin"; //$NON-NLS-1$
    }

}
