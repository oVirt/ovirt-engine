package org.ovirt.engine.ui.userportal.section.login.view;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginSectionPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoginSectionView extends AbstractView implements LoginSectionPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, LoginSectionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel loginFormPanel;

    @UiField
    Anchor vendorUrl;

    @Inject
    public LoginSectionView(ApplicationDynamicMessages dynamicMessages) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        vendorUrl.setHref(dynamicMessages.vendorUrl());
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == LoginSectionPresenter.TYPE_SetLoginForm) {
            setPanelContent(loginFormPanel, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

}
