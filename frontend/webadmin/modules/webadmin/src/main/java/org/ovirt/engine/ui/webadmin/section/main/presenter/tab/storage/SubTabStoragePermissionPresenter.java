package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
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

public class SubTabStoragePermissionPresenter
    extends AbstractSubTabStoragePresenter<PermissionListModel<StorageDomain>, SubTabStoragePermissionPresenter.ViewDef,
        SubTabStoragePermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.storagePermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabStoragePermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StorageDomain> {
    }

    @TabInfo(container = StorageSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.STORAGE_PERMISSIONS;
    }

    @Inject
    public SubTabStoragePermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, StorageMainSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, StorageListModel,
            PermissionListModel<StorageDomain>> modelProvider) {
        // View has no action panel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                StorageSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
