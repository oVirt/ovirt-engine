package org.ovirt.engine.ui.webadmin.view;

import org.ovirt.engine.ui.common.presenter.ErrorPopupPresenterWidget;
import org.ovirt.engine.ui.common.view.AbstractPopupView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.inject.Inject;

public class ErrorPopupView extends AbstractPopupView<DialogBox> implements ErrorPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<DialogBox, ErrorPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Label titleLabel;

    @UiField
    HTML messageLabel;

    @UiField
    PushButton closeButton;

    ApplicationMessages messages;
    ApplicationTemplates templates;

    @Inject
    public ErrorPopupView(EventBus eventBus, ApplicationResources resources,
            ApplicationConstants constants, ApplicationMessages messages,
            ApplicationTemplates templates) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        this.messages = messages;
        this.templates = templates;
    }

    void localize(ApplicationConstants constants) {
        titleLabel.setText(constants.errorPopupCaption());
        closeButton.setText(constants.closeButtonLabel());
    }

    @Override
    public void setErrorMessage(String errorMessage) {

        messageLabel.setHTML(errorMessage);
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }

}
