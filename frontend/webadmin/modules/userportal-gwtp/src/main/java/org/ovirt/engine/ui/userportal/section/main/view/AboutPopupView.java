package org.ovirt.engine.ui.userportal.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.AboutPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class AboutPopupView extends AbstractPopupView<DialogBox> implements AboutPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<DialogBox, AboutPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Label titleLabel;

    @UiField
    InlineLabel versionLabel;

    @UiField
    SimpleDialogButton closeButton;

    @UiField
    Label copyrightNotice;

    @Inject
    public AboutPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
    }

    void localize(ApplicationConstants constants) {
        titleLabel.setText(constants.aboutPopupCaption());
        closeButton.setText(constants.closeButtonLabel());
        copyrightNotice.setText(constants.copyRightNotice());
    }

    @Override
    public void setVersion(String text) {
        versionLabel.setText(text);
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }

}
