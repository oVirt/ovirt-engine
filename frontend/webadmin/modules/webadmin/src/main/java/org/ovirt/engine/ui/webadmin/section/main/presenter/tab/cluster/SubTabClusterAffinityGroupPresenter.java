package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.ClusterAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.AffinityGroupActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabClusterAffinityGroupPresenter
    extends AbstractSubTabClusterPresenter<ClusterAffinityGroupListModel, SubTabClusterAffinityGroupPresenter.ViewDef,
        SubTabClusterAffinityGroupPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.clusterAffinityGroupsSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabClusterAffinityGroupPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Cluster> {
    }

    @TabInfo(container = ClusterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.CLUSTER_AFFINITY_GROUP;
    }

    @Inject
    public SubTabClusterAffinityGroupPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ClusterMainSelectedItems selectedItems,
            AffinityGroupActionPanelPresenterWidget<Cluster, ClusterListModel<Void>, ClusterAffinityGroupListModel> actionPanel,
            SearchableDetailModelProvider<AffinityGroup, ClusterListModel<Void>,
            ClusterAffinityGroupListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                ClusterSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
