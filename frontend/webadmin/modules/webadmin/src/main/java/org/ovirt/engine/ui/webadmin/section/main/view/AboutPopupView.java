package org.ovirt.engine.ui.webadmin.section.main.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.ovirt.engine.ui.common.view.AbstractPopupView;
import org.ovirt.engine.ui.common.widget.dialog.OVirtBootstrapModal;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AboutPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class AboutPopupView extends AbstractPopupView<OVirtBootstrapModal> implements AboutPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<OVirtBootstrapModal, AboutPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Button closeIconButton;

    @UiField
    Label applicationTitle;

    @UiField
    Label versionText;

    @UiField
    Label userNameText;

    @UiField
    Label copyrightNotice;

    @UiField
    Anchor vendorUrl;

    @UiField
    Div logo;

    private final ApplicationDynamicMessages dynamicMessages;

    @Inject
    public AboutPopupView(EventBus eventBus, ApplicationDynamicMessages dynamicMessages) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        this.dynamicMessages = dynamicMessages;
        localize();
    }

    void localize() {
        applicationTitle.setText(dynamicMessages.applicationTitle());
        copyrightNotice.setText(dynamicMessages.copyRightNotice());
        vendorUrl.setText(dynamicMessages.vendorUrl());
        vendorUrl.setHref(dynamicMessages.vendorUrl());
    }

    @Override
    public void setVersion(String text) {
        versionText.setText(text);
    }

    @Override
    public void setUserName(String userName) {
        userNameText.setText(userName);
    }

    @Override
    public HasClickHandlers getCloseIconButton() {
        return closeIconButton;
    }

    @Override
    public HandlerRegistration setPopupKeyPressHandler(PopupNativeKeyPressHandler handler) {
        return asWidget().setKeyPressHandler(handler);
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return null;
    }

}
