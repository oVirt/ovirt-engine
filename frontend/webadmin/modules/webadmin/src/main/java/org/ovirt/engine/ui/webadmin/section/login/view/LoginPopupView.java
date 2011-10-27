package org.ovirt.engine.ui.webadmin.section.login.view;

import java.util.Arrays;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.login.presenter.LoginPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.system.InternalConfiguration;
import org.ovirt.engine.ui.webadmin.uicommon.ClientAgentType;
import org.ovirt.engine.ui.webadmin.view.AbstractPopupView;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.inject.Inject;

public class LoginPopupView extends AbstractPopupView<DecoratedPopupPanel> implements LoginPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<LoginModel, LoginPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<DecoratedPopupPanel, LoginPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path("userName.entity")
    EntityModelTextBoxEditor userNameEditor;

    @UiField
    @Path("password.entity")
    EntityModelPasswordBoxEditor passwordEditor;

    @UiField
    @Path("domain.selectedItem")
    ListModelListBoxEditor<Object> domainEditor;

    @UiField
    PushButton loginButton;

    @UiField
    @Ignore
    Label errorMessage;

    @UiField
    @Ignore
    Label footerWarningMessage;

    @Inject
    public LoginPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ClientAgentType clientAgentType,
            InternalConfiguration intConf,
            ApplicationMessages appMessages) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        asWidget().setGlassEnabled(false);
        localize(constants);
        passwordEditor.setAutoComplete("off");

        if ((!intConf.getSupportedBrowsers().containsKey(clientAgentType.browser))
                || (!Arrays.asList(intConf.getSupportedBrowsers().get(clientAgentType.browser))
                        .contains(clientAgentType.version)))
            // Browser is not supported
            footerWarningMessage.setText(appMessages.browserNotSupportedVersion(clientAgentType.browser,
                    clientAgentType.version.toString()));

        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        userNameEditor.setLabel(constants.loginFormUserNameLabel());
        passwordEditor.setLabel(constants.loginFormPasswordLabel());
        domainEditor.setLabel(constants.loginFormDomainLabel());
        loginButton.setText(constants.loginButtonLabel());
    }

    @Override
    public void edit(LoginModel object) {
        // Activate Login on click
        final UICommand loginCommand = object.getLoginCommand();
        loginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loginCommand.Execute();
            }
        });

        // Workaround: add Login Command to the Commands List
        // This is required by the Editor framework
        loginCommand.setIsDefault(true);
        object.getCommands().add(loginCommand);

        Driver.driver.edit(object);
    }

    @Override
    public LoginModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void resetAndFocus() {
        userNameEditor.asValueBox().selectAll();
        userNameEditor.asValueBox().setFocus(true);
        clearErrorMessage();
    }

    @Override
    public void setErrorMessage(String text) {
        errorMessage.setText(text);
        errorMessage.setVisible(text != null);
    }

    @Override
    public void clearErrorMessage() {
        setErrorMessage(null);
    }
}
