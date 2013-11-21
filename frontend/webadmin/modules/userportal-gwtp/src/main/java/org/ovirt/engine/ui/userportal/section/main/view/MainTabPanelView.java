package org.ovirt.engine.ui.userportal.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractTabPanelView;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.userportal.widget.tab.HeadlessTabPanel;

public class MainTabPanelView extends AbstractTabPanelView implements MainTabPanelPresenter.ViewDef {

    private final HeadlessTabPanel tabPanel = new HeadlessTabPanel();

    public MainTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return MainTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

    @Override
    public void setTabWidgetHandler(TabWidgetHandler tabWidgetHandler) {
        tabPanel.setTabWidgetHandler(tabWidgetHandler);
    }

}
