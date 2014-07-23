package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterQosSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.HyperlinkTabPanel;

public class DataCenterQosSubTabPanelView extends AbstractSubTabPanelView implements DataCenterQosSubTabPanelPresenter.ViewDef {

    private final HyperlinkTabPanel tabPanel = new HyperlinkTabPanel();

    public DataCenterQosSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return DataCenterQosSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }
}
