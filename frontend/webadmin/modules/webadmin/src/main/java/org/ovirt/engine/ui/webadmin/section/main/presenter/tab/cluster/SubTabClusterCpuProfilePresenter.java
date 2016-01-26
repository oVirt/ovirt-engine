package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
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

public class SubTabClusterCpuProfilePresenter
    extends AbstractSubTabClusterPresenter<CpuProfileListModel, SubTabClusterCpuProfilePresenter.ViewDef,
        SubTabClusterCpuProfilePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.clusterCpuProfileSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabClusterCpuProfilePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Cluster> {

        void addModelListeners();
    }

    @TabInfo(container = ClusterSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<CpuProfile, ClusterListModel<Void>, CpuProfileListModel> modelProvider) {
        return new ModelBoundTabData(constants.cpuProfilesSubTabLabel(), 7,
                modelProvider);
    }

    @Inject
    public SubTabClusterCpuProfilePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ClusterMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<CpuProfile, ClusterListModel<Void>, CpuProfileListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                ClusterSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    public void initializeHandlers() {
        super.initializeHandlers();

        getView().addModelListeners();
    }
}
