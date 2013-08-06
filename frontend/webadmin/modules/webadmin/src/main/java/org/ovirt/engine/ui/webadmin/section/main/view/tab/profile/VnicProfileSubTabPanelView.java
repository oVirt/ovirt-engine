package org.ovirt.engine.ui.webadmin.section.main.view.tab.profile;

import org.ovirt.engine.ui.common.view.AbstractTabPanelView;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.VnicProfileSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

public class VnicProfileSubTabPanelView extends AbstractTabPanelView implements VnicProfileSubTabPanelPresenter.ViewDef {

    private final SimpleTabPanel tabPanel = new SimpleTabPanel();

    public VnicProfileSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return VnicProfileSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
