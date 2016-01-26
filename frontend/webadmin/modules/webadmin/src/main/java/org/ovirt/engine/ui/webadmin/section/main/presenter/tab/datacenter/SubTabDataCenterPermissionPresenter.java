package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
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

public class SubTabDataCenterPermissionPresenter
    extends AbstractSubTabDataCenterPresenter<PermissionListModel<StoragePool>,
        SubTabDataCenterPermissionPresenter.ViewDef, SubTabDataCenterPermissionPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<Permission, DataCenterListModel,
            PermissionListModel<StoragePool>> modelProvider) {
        return new ModelBoundTabData(constants.dataCenterPermissionSubTabLabel(), 5, modelProvider);
    }

    @Inject
    public SubTabDataCenterPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DataCenterMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, DataCenterListModel,
            PermissionListModel<StoragePool>> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
