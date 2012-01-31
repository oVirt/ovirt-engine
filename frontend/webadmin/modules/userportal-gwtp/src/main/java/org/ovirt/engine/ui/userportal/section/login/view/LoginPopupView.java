package org.ovirt.engine.ui.userportal.section.login.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.common.view.AbstractPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalLoginModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.userportal.widget.dialog.SimplePopupPanel;

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
    EntityModelCheckBoxEditor loginAutomatically;

    @Inject
    public LoginPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages) {
        super(eventBus, resources);
        loginAutomatically = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initLoginAutomatically();
        asWidget().setGlassEnabled(false);
        localize(constants);
        passwordEditor.setAutoComplete("off");
        Driver.driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    protected void initLoginAutomatically() {
        loginAutomatically.setLabel("Connect Automatically");
    }

    void localize(ApplicationConstants constants) {
        userNameEditor.setLabel(constants.loginFormUserNameLabel());
        passwordEditor.setLabel(constants.loginFormPasswordLabel());
        domainEditor.setLabel(constants.loginFormDomainLabel());
        loginButton.setText(constants.loginButtonLabel());
    }

    @Override
    public void edit(UserPortalLoginModel object) {
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
}
