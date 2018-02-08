package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;

public class ClusterSubTabPanelPresenter extends AbstractSubTabPanelPresenter<ClusterSubTabPanelPresenter.ViewDef,
    ClusterSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<ClusterSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    public static final NestedSlot TYPE_SetTabContent = new NestedSlot();

    @Inject
    private MainModelProvider<Cluster, ClusterListModel<Void>> modelProvider;

    @Inject
    public ClusterSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            ClusterMainSelectedItems selectedItems) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, selectedItems);
    }

    @Override
    protected void initDetailTabToModelMapping(Map<TabData, Model> mapping) {
        ClusterListModel<Void> mainModel = modelProvider.getModel();
        mapping.put(DetailTabDataIndex.CLUSTER_GENERAL, mainModel.getGeneralModel());
        mapping.put(DetailTabDataIndex.CLUSTER_NETWORK, mainModel.getNetworkListModel());
        mapping.put(DetailTabDataIndex.CLUSTER_HOST, mainModel.getHostListModel());
        mapping.put(DetailTabDataIndex.CLUSTER_VM, mainModel.getVmListModel());
        mapping.put(DetailTabDataIndex.CLUSTER_SERVICE, mainModel.getServiceModel());
        mapping.put(DetailTabDataIndex.CLUSTER_GLUSTER_HOOKS, mainModel.getGlusterHookListModel());
        mapping.put(DetailTabDataIndex.CLUSTER_AFFINITY_GROUP, mainModel.getAffinityGroupListModel());
        mapping.put(DetailTabDataIndex.CLUSTER_AFFINITY_LABEL, mainModel.getAffinityLabelListModel());
        mapping.put(DetailTabDataIndex.CLUSTER_CPU_PROFILES, mainModel.getCpuProfileListModel());
        mapping.put(DetailTabDataIndex.CLUSTER_PERMISSIONS, mainModel.getPermissionListModel());
    }

}
