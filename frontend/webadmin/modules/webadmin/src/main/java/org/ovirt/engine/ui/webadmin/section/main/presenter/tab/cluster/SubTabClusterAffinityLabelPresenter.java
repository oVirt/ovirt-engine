package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.ClusterAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabClusterAffinityLabelPresenter
        extends AbstractSubTabClusterPresenter<ClusterAffinityLabelListModel, SubTabClusterAffinityLabelPresenter.ViewDef,
        SubTabClusterAffinityLabelPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.clusterAffinityLabelsSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabClusterAffinityLabelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Cluster> {
    }

    @TabInfo(container = ClusterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.CLUSTER_AFFINITY_LABEL;
    }

    @Inject
    public SubTabClusterAffinityLabelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
           PlaceManager placeManager, ClusterMainSelectedItems selectedItems,
           SearchableDetailModelProvider<Label, ClusterListModel<Void>, ClusterAffinityLabelListModel> modelProvider,
           ClusterAffinityLabelActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                ClusterSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
