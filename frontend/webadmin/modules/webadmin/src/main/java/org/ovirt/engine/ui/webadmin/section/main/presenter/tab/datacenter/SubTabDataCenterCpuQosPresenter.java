package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterCpuQosListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabDataCenterCpuQosPresenter extends AbstractSubTabPresenter<StoragePool, DataCenterListModel,
        DataCenterCpuQosListModel, SubTabDataCenterCpuQosPresenter.ViewDef,
        SubTabDataCenterCpuQosPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterCpuQosSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterCpuQosPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterQosSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.dataCenterCpuQosSubTabLabel(), 3, modelProvider);
    }

    @Inject
    public SubTabDataCenterCpuQosPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                DataCenterQosSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.dataCenterMainTabPlace);
    }

    @ProxyEvent
    public void onDataCenterSelectionChange(DataCenterSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
