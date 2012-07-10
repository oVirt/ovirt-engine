package org.ovirt.engine.ui.userportal.section.login.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractLoginPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.DialogBoxWithKeyHandlers;
import org.ovirt.engine.ui.common.widget.dialog.SimplePopupPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalLoginModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.inject.Inject;

public class LoginPopupView extends AbstractLoginPopupView implements LoginPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<UserPortalLoginModel, LoginPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<DialogBoxWithKeyHandlers, LoginPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<LoginPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    SimplePopupPanel popup;

    @UiField
    @Ignore
    Label headerLabel;

    @UiField(provided = true)
    @Path("userName.entity")
    @WithElementId("userName")
    EntityModelTextBoxEditor userNameEditor;

    @UiField
    @Path("password.entity")
    @WithElementId("password")
    EntityModelPasswordBoxEditor passwordEditor;

    @UiField
    @Path("domain.selectedItem")
    @WithElementId("domain")
    ListModelListBoxEditor<Object> domainEditor;

    @UiField
    @Ignore
    Label userNameLabel;

    @UiField
    @Ignore
    Label passwordLabel;

    @UiField
    @Ignore
    Label domainLabel;

    @UiField
    @WithElementId
    UiCommandButton loginButton;

    @UiField
    @Ignore
    Label errorMessage;

    @UiField(provided = true)
    @Path("isAutoConnect.entity")
    @WithElementId
    EntityModelCheckBoxEditor connectAutomatically;

    @UiField
    @Ignore
    Panel errorMessagePanel;

    @Inject
    public LoginPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages) {
        super(eventBus, resources);

        // We need this code because resetAndFocus is called when userNameEditor is Disabled
        userNameEditor = new EntityModelTextBoxEditor() {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                if (enabled) {
                    userNameEditor.asValueBox().selectAll();
                    userNameEditor.setFocus(true);
                }
            }
        };

        connectAutomatically = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        asWidget().setGlassEnabled(false);
        localize(constants);
        addStyles();

        errorMessagePanel.setVisible(false);
        passwordEditor.setAutoComplete("off"); //$NON-NLS-1$

        ViewIdHandler.idHandler.generateAndSetIds(this);
        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        headerLabel.setText(constants.loginHeaderLabel());
        userNameLabel.setText(constants.loginFormUserNameLabel());
        passwordLabel.setText(constants.loginFormPasswordLabel());
        domainLabel.setText(constants.loginFormDomainLabel());
        connectAutomatically.setLabel(constants.loginFormConnectAutomaticallyLabel());
        loginButton.setLabel(constants.loginButtonLabel());
    }

    private void addStyles() {
        userNameEditor.hideLabel();
        passwordEditor.hideLabel();
        domainEditor.hideLabel();
    }

    @Override
    public void edit(UserPortalLoginModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public UserPortalLoginModel flush() {
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
        if (errorMessage.isVisible()) {
            errorMessagePanel.setVisible(true);
        }
    }

    @Override
    public void clearErrorMessage() {
        setErrorMessage(null);
    }

    @Override
    public HasUiCommandClickHandlers getLoginButton() {
        return loginButton;
    }

}
