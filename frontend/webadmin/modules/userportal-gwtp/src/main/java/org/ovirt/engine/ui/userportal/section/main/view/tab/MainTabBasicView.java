package org.ovirt.engine.ui.userportal.section.main.view.tab;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabBasicPresenter;

import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class MainTabBasicView extends AbstractView implements MainTabBasicPresenter.ViewDef {

    @Inject
    public MainTabBasicView() {
        initWidget(new Label("TODO MainTabBasicView"));
    }

}
