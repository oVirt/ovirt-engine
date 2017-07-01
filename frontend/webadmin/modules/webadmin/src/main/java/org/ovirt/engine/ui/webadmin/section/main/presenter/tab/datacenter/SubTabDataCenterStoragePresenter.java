package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
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

public class SubTabDataCenterStoragePresenter
    extends AbstractSubTabDataCenterPresenter<DataCenterStorageListModel, SubTabDataCenterStoragePresenter.ViewDef,
        SubTabDataCenterStoragePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterStorageSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterStoragePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return new GroupedTabData(constants.dataCenterStorageSubTabLabel(), 0);
    }

    @Inject
    public SubTabDataCenterStoragePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DataCenterMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
