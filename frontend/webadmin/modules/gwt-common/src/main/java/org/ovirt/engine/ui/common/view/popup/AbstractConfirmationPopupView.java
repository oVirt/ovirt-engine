package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;

public abstract class AbstractConfirmationPopupView extends AbstractModelBoundPopupView<ConfirmationModel> {

    @UiField
    @Ignore
    public HTML messageHTML;

    public AbstractConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void setMessage(String message) {
        messageHTML.setHTML(SafeHtmlUtils.fromString(message != null ? message : "").asString().replace("\n", "<br>"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
