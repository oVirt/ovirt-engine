package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterServiceModel;
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

public class SubTabClusterServicePresenter
    extends AbstractSubTabClusterPresenter<ClusterServiceModel, SubTabClusterServicePresenter.ViewDef,
        SubTabClusterServicePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.clusterServiceSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabClusterServicePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Cluster> {
    }

    @TabInfo(container = ClusterSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<ClusterListModel<Void>, ClusterServiceModel> modelProvider) {
        return new ModelBoundTabData(constants.clusterServiceSubTabLabel(), 4, modelProvider);
    }

    @Inject
    public SubTabClusterServicePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            ClusterMainTabSelectedItems selectedItems, PlaceManager placeManager,
            DetailModelProvider<ClusterListModel<Void>, ClusterServiceModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                ClusterSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
