package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.ExtendedVmSubTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.userportal.widget.tab.SimpleTabPanel;

public class ExtendedVmSubTabPanelView extends AbstractSubTabPanelView implements
    ExtendedVmSubTabPanelPresenter.ViewDef {

    private final SimpleTabPanel tabPanel = new SimpleTabPanel();

    public ExtendedVmSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return ExtendedVmSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }
}
