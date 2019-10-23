package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ClusterActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class ClusterSubTabPanelView extends AbstractSubTabPanelView implements ClusterSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<ClusterSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ClusterActionPanelPresenterWidget actionPanel;
    private final SimpleTabPanel tabPanel;

    @Inject
    public ClusterSubTabPanelView(OvirtBreadCrumbsPresenterWidget<Cluster, ClusterListModel<Void>> breadCrumbs,
            ClusterActionPanelPresenterWidget actionPanel, DetailTabLayout detailTabLayout) {
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
        return ClusterSubTabPanelPresenter.TYPE_SetTabContent;
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
