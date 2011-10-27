package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.view.AbstractTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.widget.tab.HeadlessTabPanel;
import org.ovirt.engine.ui.webadmin.widget.tab.HeadlessTabPanel.TabWidgetHandler;

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
