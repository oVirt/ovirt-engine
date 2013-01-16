package org.ovirt.engine.ui.webadmin.section.main.view.tab.provider;

import org.ovirt.engine.ui.common.view.AbstractTabPanelView;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.ProviderSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

public class ProviderSubTabPanelView extends AbstractTabPanelView implements ProviderSubTabPanelPresenter.ViewDef {

    private final SimpleTabPanel tabPanel = new SimpleTabPanel();

    public ProviderSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return ProviderSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}

