package org.ovirt.engine.ui.userportal.main.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.userportal.view.popup.UserPortalModelBoundPopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

public abstract class AbstractConfirmationPopupView extends UserPortalModelBoundPopupView<ConfirmationModel> {

    @UiField
    @Ignore
    Label messageLabel;

    public AbstractConfirmationPopupView(EventBus eventBus, CommonApplicationResources resources) {
        super(eventBus, resources);
    }

    @Override
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

}
