package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkQoSListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterCpuQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterHostNetworkQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
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

public class SubTabDataCenterQosPresenter
    extends AbstractSubTabDataCenterPresenter<DataCenterStorageQosListModel,
        SubTabDataCenterQosPresenter.ViewDef, SubTabDataCenterQosPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterQosSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterQosPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
        void resizeContainerToHeight();
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.DATACENTER_QOS;
    }

    private SearchableDetailModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel> vmNetworkModelProvider;
    private SearchableDetailModelProvider<HostNetworkQos, DataCenterListModel, DataCenterHostNetworkQosListModel> hostNetworkModelProvider;
    private SearchableDetailModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel> cpuModelProvider;

    @Override
    public void onBind() {
        super.onBind();

        // Change the entity in all models, when the entity in the storage qos model changes.
        getModelProvider().getModel().getEntityChangedEvent().addListener((ev, sender, args) -> {
            StoragePool entity = getModelProvider().getModel().getEntity();
            vmNetworkModelProvider.getModel().setEntity(entity);
            hostNetworkModelProvider.getModel().setEntity(entity);
            cpuModelProvider.getModel().setEntity(entity);
        });

        registerHandler(getView().addWindowResizeHandler(e -> {
            getView().resizeContainerToHeight();
        }));
    }

    @Override
    public void onReveal() {
        super.onReveal();

        // Activate all model providers
        vmNetworkModelProvider.activateDetailModel();
        hostNetworkModelProvider.activateDetailModel();
        cpuModelProvider.activateDetailModel();

        getView().resizeContainerToHeight();
    }

    @Inject
    public SubTabDataCenterQosPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DataCenterMainSelectedItems selectedItems,
            DataCenterStorageQosActionPanelPresenterWidget actionPanel,
            SearchableDetailModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel> storageModelProvider,
            SearchableDetailModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel> vmNetworkModelProvider,
            SearchableDetailModelProvider<HostNetworkQos, DataCenterListModel, DataCenterHostNetworkQosListModel> hostNetworkModelProvider,
            SearchableDetailModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel> cpuModelProvider) {
        super(eventBus, view, proxy, placeManager, storageModelProvider, selectedItems, actionPanel,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);

        this.vmNetworkModelProvider = vmNetworkModelProvider;
        this.hostNetworkModelProvider = hostNetworkModelProvider;
        this.cpuModelProvider = cpuModelProvider;
    }

}
