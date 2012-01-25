package org.ovirt.engine.ui.userportal.section.main.view.tab;

import org.ovirt.engine.ui.common.view.AbstractTabPanelView;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.widget.tab.VerticalTabPanel;

public class MainTabExtendedView extends AbstractTabPanelView implements MainTabExtendedPresenter.ViewDef {

    private final VerticalTabPanel tabPanel = new VerticalTabPanel();

    public MainTabExtendedView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return MainTabExtendedPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
