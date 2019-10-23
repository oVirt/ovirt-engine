package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VirtualMachineSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class VirtualMachineSubTabPanelView extends AbstractSubTabPanelView
    implements VirtualMachineSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VirtualMachineSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final VirtualMachineActionPanelPresenterWidget actionPanel;
    private final SimpleTabPanel tabPanel;

    @Inject
    public VirtualMachineSubTabPanelView(OvirtBreadCrumbsPresenterWidget<VM, VmListModel<Void>> breadCrumbs,
            VirtualMachineActionPanelPresenterWidget actionPanel, DetailTabLayout detailTabLayout) {
        this.actionPanel = actionPanel;
        this.tabPanel = new SimpleTabPanel(breadCrumbs, actionPanel, detailTabLayout);
        initWidget(getTabPanel());
        actionPanel.removeButton(actionPanel.getNewButtonDefinition());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected Object getContentSlot() {
        return VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

    @Override
    public ActionPanelPresenterWidget<?, ?, ?> getActionPanelPresenterWidget() {
        return actionPanel;
    }

}
