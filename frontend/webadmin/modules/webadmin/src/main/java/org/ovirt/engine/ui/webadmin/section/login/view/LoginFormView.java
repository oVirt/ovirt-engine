package org.ovirt.engine.ui.webadmin.section.login.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.AbstractLoginFormView;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.login.presenter.LoginFormPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.inject.Inject;

public class LoginFormView extends AbstractLoginFormView implements LoginFormPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<LoginModel, LoginFormView> {
    }

    interface ViewUiBinder extends UiBinder<FocusPanel, LoginFormView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<LoginFormView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public LoginFormView(EventBus eventBus,
            ApplicationDynamicMessages dynamicMessages) {
        super(eventBus);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        setStyles();
        driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void localize() {
        userNameEditor.setPlaceHolder(constants.loginFormUserNameLabel());
        passwordEditor.setPlaceHolder(constants.loginFormPasswordLabel());
        loginButton.setLabel(constants.loginButtonLabel());
    }

    @Override
    public void edit(LoginModel object) {
        driver.edit(object);
    }

    @Override
    public LoginModel flush() {
        return driver.flush();
    }

}
