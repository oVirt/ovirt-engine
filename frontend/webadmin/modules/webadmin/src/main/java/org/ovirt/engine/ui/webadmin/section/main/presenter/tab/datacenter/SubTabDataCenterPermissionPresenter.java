package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
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

public class SubTabDataCenterPermissionPresenter
    extends AbstractSubTabDataCenterPresenter<PermissionListModel<StoragePool>,
        SubTabDataCenterPermissionPresenter.ViewDef, SubTabDataCenterPermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.DATACENTER_PERMISSIONS;
    }

    @Inject
    public SubTabDataCenterPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DataCenterMainSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, DataCenterListModel, PermissionListModel<StoragePool>> modelProvider) {
        // View uses PermissionWithInheritedPermissionListModelTable to get action panel elsewhere passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
