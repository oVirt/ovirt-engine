package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class HostSubTabPanelView extends AbstractSubTabPanelView implements HostSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<HostSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final HostActionPanelPresenterWidget actionPanel;
    private final SimpleTabPanel tabPanel;

    @Inject
    public HostSubTabPanelView(OvirtBreadCrumbsPresenterWidget<VDS, HostListModel<Void>> breadCrumbs,
            HostActionPanelPresenterWidget actionPanel, DetailTabLayout detailTabLayout) {
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
        return HostSubTabPanelPresenter.TYPE_SetTabContent;
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
