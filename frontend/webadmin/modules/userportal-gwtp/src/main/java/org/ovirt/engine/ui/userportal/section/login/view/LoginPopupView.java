package org.ovirt.engine.ui.userportal.section.login.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
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
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalLoginModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

public class LoginPopupView extends AbstractLoginPopupView implements LoginPopupPresenterWidget.ViewDef {

    private final ApplicationTemplates templates;

    interface Driver extends SimpleBeanEditorDriver<UserPortalLoginModel, LoginPopupView> {
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

    @Ignore
    SimplePopupPanel tooltipPanel;

    @Ignore
    HTML tooltip;

    @UiField
    Style style;

    private final Driver driver = GWT.create(Driver.class);

    private int popupLeft = 0;
    private int popupTop = 0;

    // This is true by default. If no message exists then the tooltip is hidden
    private boolean hasMessageOfTheDay = true;

    @Inject
    public LoginPopupView(EventBus eventBus,
            ClientAgentType clientAgentType,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages,
            ApplicationDynamicMessages dynamicMessages,
            ApplicationTemplates templates) {
        super(eventBus, resources, clientAgentType);

        this.templates = templates;

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
        localize(constants, dynamicMessages);
        addStyles();

        errorMessagePanel.setVisible(false);
        passwordEditor.setAutoComplete("off"); //$NON-NLS-1$

        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initUserMessageOfTheDayToolTip(final ApplicationTemplates templates) {
        tooltipPanel = new SimplePopupPanel();
        tooltipPanel.setStyleName(style.motd());
        tooltipPanel.hide();
        tooltip = new HTML();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                String message = (String) result;

                if (message != null && !message.isEmpty()) {
                    tooltip.setHTML(templates.userMessageOfTheDay(message));
                    tooltipPanel.setWidget(tooltip);
                } else {
                    tooltipPanel.hide();
                    hasMessageOfTheDay = false;
                }

            }
        };
        AsyncDataProvider.getUserMessageOfTheDayViaPublic(_asyncQuery);

    }

    private void setToolTipPositionAndShow() {
        // We set those as the absoulute left and top values
        // are different in subsequent calls, so we anchor them to the first ones that aren't zero
        // It is a workaround to a strange behavior
        if (popupLeft == 0) {
            popupLeft = popup.getAbsoluteLeft();
        }

        if (popupTop == 0) {
            popupTop = popup.getAbsoluteTop();
        }

        if (hasMessageOfTheDay) {
            final int errorHeight = errorMessagePanel.isVisible() ? errorMessage.getOffsetHeight() - 2 : 0;
            tooltipPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    // Putting the tooltip in the center of the login dialog, right underneath it
                    tooltipPanel.setPopupPosition(popupLeft - (offsetWidth / 2), popupTop + (popup.getOffsetHeight() / 2 + errorHeight));
                }
            });
        }
    }

    void localize(ApplicationConstants constants,
            ApplicationDynamicMessages dynamicMessages) {
        headerLabel.setText(dynamicMessages.loginHeaderLabel());
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
        driver.edit(object);

        // Initializing relevant widgets of the user message of the day
        initUserMessageOfTheDayToolTip(templates);
    }

    @Override
    public UserPortalLoginModel flush() {
        return driver.flush();
    }

    @Override
    public void resetAndFocus() {
        userNameEditor.asValueBox().selectAll();
        userNameEditor.asValueBox().setFocus(true);
        clearErrorMessage();
        setToolTipPositionAndShow();
    }

    @Override
    public void setErrorMessage(String text) {
        errorMessage.setText(text);
        errorMessage.setVisible(text != null);
        if (errorMessage.isVisible()) {
            errorMessagePanel.setVisible(true);
            // In case an error message is displayed, we need to recalculate the position of the
            // user message of the day tool tip
            setToolTipPositionAndShow();
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

    public interface Style extends CssResource {

        String motd();

    }
}
