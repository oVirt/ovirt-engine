package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
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

public class SubTabDataCenterClusterPresenter
    extends AbstractSubTabDataCenterPresenter<DataCenterClusterListModel, SubTabDataCenterClusterPresenter.ViewDef,
        SubTabDataCenterClusterPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterClusterSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterClusterPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.DATACENTER_CLUSTERS;
    }

    @Inject
    public SubTabDataCenterClusterPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DataCenterMainSelectedItems selectedItems,
            SearchableDetailModelProvider<Cluster, DataCenterListModel, DataCenterClusterListModel> modelProvider) {
        // View has no action panel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
