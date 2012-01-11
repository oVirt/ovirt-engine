package org.ovirt.engine.ui.webadmin.section.login.presenter;

import java.util.logging.Logger;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.auth.SilentLoginData;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class LoginPopupPresenterWidget extends PresenterWidget<LoginPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends PopupView, HasEditorDriver<LoginModel> {

        void resetAndFocus();

        void setErrorMessage(String text);

        void clearErrorMessage();

    }

    private static final Logger logger = Logger.getLogger(LoginPopupPresenterWidget.class.getName());

    private SilentLoginData silentLoginData;

    @Inject
    public LoginPopupPresenterWidget(EventBus eventBus, ViewDef view, LoginModel loginModel) {
        super(eventBus, view);
        getView().edit(loginModel);
    }

    public void setSilentLoginData(SilentLoginData silentLoginData) {
        this.silentLoginData = silentLoginData;
    }

    @Override
    protected void onBind() {
        super.onBind();

        final LoginModel loginModel = getView().flush();
        loginModel.getLoggedInEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().clearErrorMessage();
            }
        });

        loginModel.getLoginFailedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                logger.warning("Login failed for user [" + loginModel.getUserName().getEntity() + "]");
                getView().setErrorMessage(loginModel.getMessage());

                // FIXME: Re-enable login properties (can't this be handled by the model itself when auth fails?)
                loginModel.getUserName().setIsChangable(true);
                loginModel.getPassword().setIsChangable(true);
                loginModel.getDomain().setIsChangable(true);
                loginModel.getLoginCommand().setIsExecutionAllowed(true);
            }
        });
    }

    @Override
    protected void onReset() {
        super.onReset();

        if (silentLoginData != null) {
            performSilentLogin();
        }

        getView().resetAndFocus();
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

}
