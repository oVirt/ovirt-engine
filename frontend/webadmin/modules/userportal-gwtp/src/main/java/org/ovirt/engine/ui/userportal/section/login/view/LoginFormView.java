package org.ovirt.engine.ui.userportal.section.login.view;

import org.gwtbootstrap3.client.ui.Label;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractLoginFormView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalLoginModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginFormPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.inject.Inject;

public class LoginFormView extends AbstractLoginFormView implements LoginFormPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<UserPortalLoginModel, LoginFormView> {
    }

    interface ViewUiBinder extends UiBinder<FocusPanel, LoginFormView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<LoginFormView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Path("isAutoConnect.entity")
    @WithElementId
    EntityModelCheckBoxEditor connectAutomaticallyEditor;

    @UiField
    @Ignore
    Panel motdPanel;

    @UiField
    @Ignore
    Label motdHeaderLabel;

    @UiField
    @Ignore
    Panel motdMessagePanel;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public LoginFormView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages,
            ApplicationDynamicMessages dynamicMessages,
            ApplicationTemplates templates) {
        super(eventBus, resources);

        connectAutomaticallyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants, dynamicMessages);

        setStyles();

        retrieveMessageOfTheDay(templates);

        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    protected void setStyles() {
        super.setStyles();
        connectAutomaticallyEditor.setContentWidgetContainerStyleName("connect-automatically-checkbox"); //$NON-NLS-1$
        connectAutomaticallyEditor.addContentWidgetContainerStyleName("connect-automatically-checkbox_pfly_fix"); //$NON-NLS-1$
        motdPanel.setVisible(false);
    }

    private void retrieveMessageOfTheDay(final ApplicationTemplates templates) {

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                String message = (String) result;

                if (message != null && !message.isEmpty()) {
                    motdMessagePanel.clear();
                    motdMessagePanel.add(new HTMLPanel(templates.userMessageOfTheDay(message)));
                    motdPanel.setVisible(true);
                } else {
                    motdPanel.setVisible(false);
                }

            }
        };
        AsyncDataProvider.getInstance().getUserMessageOfTheDayViaPublic(_asyncQuery);

    }

    void localize(ApplicationConstants constants,
            ApplicationDynamicMessages dynamicMessages) {
        userNameEditor.setLabel(constants.loginFormUserNameLabel());
        passwordEditor.setLabel(constants.loginFormPasswordLabel());
        profileEditor.setLabel(constants.loginFormProfileLabel());
        connectAutomaticallyEditor.setLabel(constants.loginFormConnectAutomaticallyLabel());
        loginButton.setLabel(constants.loginButtonLabel());
        motdHeaderLabel.setText(constants.motdHeaderLabel());
    }

    @Override
    public void edit(UserPortalLoginModel object) {
        driver.edit(object);
    }

    @Override
    public UserPortalLoginModel flush() {
        return driver.flush();
    }

}
