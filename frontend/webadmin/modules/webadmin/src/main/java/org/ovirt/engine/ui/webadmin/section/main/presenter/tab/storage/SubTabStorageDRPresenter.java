package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDRListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

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

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.storageDRSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabStorageDRPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StorageDomain> {
        void showErrorMessage(SafeHtml errorMessage);

        void clearErrorMessage();
    }

    @TabInfo(container = StorageSubTabPanelPresenter.class)
    static TabData getTabData(SearchableDetailModelProvider<StorageDomainDR, StorageListModel, StorageDRListModel> modelProvider) {
        return new ModelBoundTabData(constants.storageDRSubTabLabel(), 11, modelProvider);
    }

    @Inject
    public SubTabStorageDRPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, StorageMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<StorageDomainDR, StorageListModel, StorageDRListModel> modelProvider) {
        super(eventBus,
                view,
                proxy,
                placeManager,
                modelProvider,
                selectedItems,
                StorageSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
