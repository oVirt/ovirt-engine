package org.ovirt.engine.ui.webadmin.section.login.view;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.login.presenter.LoginPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.system.InternalConfiguration;
import org.ovirt.engine.ui.webadmin.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.webadmin.view.AbstractPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimplePopupPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class LoginPopupView extends AbstractPopupView<DecoratedPopupPanel> implements LoginPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<LoginModel, LoginPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<DecoratedPopupPanel, LoginPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePopupPanel popup;

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
    SimpleDialogButton loginButton;

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
            InternalConfiguration intConf,
            ApplicationMessages messages) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        asWidget().setGlassEnabled(false);
        localize(constants);
        passwordEditor.setAutoComplete("off");
        Driver.driver.initialize(this);

        if (!intConf.isCurrentBrowserSupported()) {
            // Browser is not supported
            footerWarningMessage.setText(messages.browserNotSupportedVersion(
                    intConf.getCurrentBrowser(),
                    intConf.getCurrentBrowserVersion()));
        }
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

        // Add popup key handlers
        final DeferredModelCommandInvoker commandInvoker = new DeferredModelCommandInvoker(object);
        popup.setKeyPressHandler(new PopupNativeKeyPressHandler() {
            @Override
            public void onKeyPress(NativeEvent event) {
                if (KeyCodes.KEY_ENTER == event.getKeyCode()) {
                    commandInvoker.invokeDefaultCommand();
                }
            }
        });

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
