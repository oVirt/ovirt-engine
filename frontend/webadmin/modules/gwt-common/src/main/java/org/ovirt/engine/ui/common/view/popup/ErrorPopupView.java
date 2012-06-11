package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.presenter.popup.ErrorPopupPresenterWidget;
import org.ovirt.engine.ui.common.view.AbstractPopupView;

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

    @Inject
    public ErrorPopupView(EventBus eventBus, CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
    }

    void localize(CommonApplicationConstants constants) {
        titleLabel.setText(constants.errorPopupCaption());
        closeButton.setText(constants.closeButtonLabel());
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        messageLabel.setHTML(errorMessage.replace("\n", "<br/>"));//$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }

}
