package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterDiskImageListModel;
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

public class SubTabStorageRegisterDiskImagePresenter
        extends AbstractSubTabStoragePresenter<StorageRegisterDiskImageListModel, SubTabStorageRegisterDiskImagePresenter.ViewDef,
        SubTabStorageRegisterDiskImagePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.storageDisksImageRegisterSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabStorageRegisterDiskImagePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StorageDomain> {
    }

    @TabInfo(container = StorageSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.STORAGE_REGISTER_DISK_IMAGE;
    }

    @Inject
    public SubTabStorageRegisterDiskImagePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, StorageMainSelectedItems selectedItems,
            StorageRegisterDiskImageActionPanelPresenterWidget actionPanel,
            SearchableDetailModelProvider<Disk, StorageListModel, StorageRegisterDiskImageListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                StorageSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
