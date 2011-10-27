package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AboutPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.view.AbstractPopupView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
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
    PushButton closeButton;

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
