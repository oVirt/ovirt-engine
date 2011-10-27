package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

public abstract class AbstractConfirmationPopupView extends AbstractModelBoundPopupView<ConfirmationModel> {

    @UiField
    @Ignore
    Label messageLabel;

    public AbstractConfirmationPopupView(EventBus eventBus, ApplicationResources resources) {
        super(eventBus, resources);
    }

    @Override
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    @Override
    public void focus() {
        asWidget().center();
    }

}
