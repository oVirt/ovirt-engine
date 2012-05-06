package org.ovirt.engine.ui.userportal.section.login.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractLoginPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
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
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class LoginPopupView extends AbstractLoginPopupView implements LoginPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<UserPortalLoginModel, LoginPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<DecoratedPopupPanel, LoginPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<LoginPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    SimplePopupPanel popup;

    @UiField
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
    @WithElementId
    SimpleDialogButton loginButton;

    @UiField
    @Ignore
    Label errorMessage;

    @UiField(provided = true)
    @Path("isAutoConnect.entity")
    EntityModelCheckBoxEditor connectAutomatically;

    @Inject
    public LoginPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages) {
        super(eventBus, resources);

        connectAutomatically = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        asWidget().setGlassEnabled(false);
        localize(constants);

        passwordEditor.setAutoComplete("off"); //$NON-NLS-1$
        Driver.driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void localize(ApplicationConstants constants) {
        super.localize(constants);
        userNameEditor.setLabel(constants.loginFormUserNameLabel());
        passwordEditor.setLabel(constants.loginFormPasswordLabel());
        domainEditor.setLabel(constants.loginFormDomainLabel());
        connectAutomatically.setLabel(constants.loginFormConnectAutomaticallyLabel());
        loginButton.setText(constants.loginButtonLabel());
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
    }

    @Override
    public void clearErrorMessage() {
        setErrorMessage(null);
    }

    @Override
    public HasClickHandlers getLoginButton() {
        return loginButton;
    }

    @Override
    public void setPopupKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler) {
        popup.setKeyPressHandler(keyPressHandler);
    }

}
