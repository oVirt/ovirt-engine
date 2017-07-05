package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabClusterVmPresenter
    extends AbstractSubTabClusterPresenter<ClusterVmListModel, SubTabClusterVmPresenter.ViewDef,
        SubTabClusterVmPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.clusterVmSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabClusterVmPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Cluster> {
    }

    @TabInfo(container = ClusterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return new GroupedTabData(constants.clusterVmSubTabLabel(), 3);
    }

    @Inject
    public SubTabClusterVmPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ClusterMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<VM, ClusterListModel<Void>, ClusterVmListModel> modelProvider) {
        // View doesn't have action panel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                ClusterSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
