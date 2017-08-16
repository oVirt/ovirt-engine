package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDRListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabStorageDRPresenter extends AbstractSubTabStoragePresenter<StorageDRListModel, SubTabStorageDRPresenter.ViewDef, SubTabStorageDRPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.storageDRSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabStorageDRPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StorageDomain> {
        void showErrorMessage(SafeHtml errorMessage);

        void clearErrorMessage();
    }

    @TabInfo(container = StorageSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.STORAGE_DR;
    }

    @Inject
    public SubTabStorageDRPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, StorageMainSelectedItems selectedItems,
            StorageDRActionPanelPresenterWidget actionPanel,
            SearchableDetailModelProvider<StorageDomainDR, StorageListModel, StorageDRListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                StorageSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
