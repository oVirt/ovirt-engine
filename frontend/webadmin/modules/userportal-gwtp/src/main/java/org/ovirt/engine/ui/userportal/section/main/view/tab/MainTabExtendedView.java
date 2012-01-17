package org.ovirt.engine.ui.userportal.section.main.view.tab;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;

import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class MainTabExtendedView extends AbstractView implements MainTabExtendedPresenter.ViewDef {

    @Inject
    public MainTabExtendedView() {
        initWidget(new Label("TODO MainTabExtendedView"));
    }

}
