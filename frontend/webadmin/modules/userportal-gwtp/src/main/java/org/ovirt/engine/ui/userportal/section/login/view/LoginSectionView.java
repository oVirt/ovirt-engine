package org.ovirt.engine.ui.userportal.section.login.view;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginSectionPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class LoginSectionView extends AbstractView implements LoginSectionPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, LoginSectionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public LoginSectionView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

}
