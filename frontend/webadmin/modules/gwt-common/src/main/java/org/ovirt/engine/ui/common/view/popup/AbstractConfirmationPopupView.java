package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.widget.AlertWithIcon;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;

public abstract class AbstractConfirmationPopupView extends AbstractModelBoundPopupView<ConfirmationModel> {

    @UiField
    public AlertWithIcon messagePanel;

    public AbstractConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void setMessage(String message) {
        messagePanel.setText(SafeHtmlUtils.fromString(
                message != null ? message : "").asString().replace("\n", "<br>"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
