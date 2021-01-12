package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
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

public class SubTabClusterEventPresenter
    extends AbstractSubTabClusterPresenter<ClusterEventListModel, SubTabClusterEventPresenter.ViewDef,
            SubTabClusterEventPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.clusterEventSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabClusterEventPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Cluster> {
    }

    @TabInfo(container = ClusterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.CLUSTER_EVENTS;
    }

    @Inject
    public SubTabClusterEventPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
                                       PlaceManager placeManager, ClusterMainSelectedItems selectedItems,
                                       SearchableDetailModelProvider<AuditLog, ClusterListModel<Void>, ClusterEventListModel> modelProvider) {
        // View does not have actionPanel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                ClusterSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
