package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
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

public class SubTabDataCenterQosPresenter
    extends AbstractSubTabDataCenterPresenter<DataCenterStorageQosListModel,
        SubTabDataCenterQosPresenter.ViewDef, SubTabDataCenterQosPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterQosSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterQosPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData(
          SearchableDetailModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel> modelProvider) {
        return new ModelBoundTabData(constants.dataCenterQosSubTabLabel(), 4, modelProvider);
    }

    @Inject
    public SubTabDataCenterQosPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DataCenterMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
