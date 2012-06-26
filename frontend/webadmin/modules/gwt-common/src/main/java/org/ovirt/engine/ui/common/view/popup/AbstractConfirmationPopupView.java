package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

public abstract class AbstractConfirmationPopupView extends AbstractModelBoundPopupView<ConfirmationModel> {

    @UiField
    @Ignore
    public Label messageLabel;

    public AbstractConfirmationPopupView(EventBus eventBus, CommonApplicationResources resources) {
        super(eventBus, resources);
    }

    @Override
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

}
