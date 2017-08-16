package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskStorageListModel;
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

public class SubTabDiskStoragePresenter
    extends AbstractSubTabDiskPresenter<DiskStorageListModel, SubTabDiskStoragePresenter.ViewDef,
        SubTabDiskStoragePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.diskStorageSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDiskStoragePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Disk> {
    }

    @TabInfo(container = DiskSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.DISKS_STORAGE;
    }

    @Inject
    public SubTabDiskStoragePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DiskMainSelectedItems selectedItems,
            SearchableDetailModelProvider<StorageDomain, DiskListModel, DiskStorageListModel> modelProvider) {
        // View has no action panel, passing null
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                DiskSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
