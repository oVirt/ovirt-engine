package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.StorageActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainStoragePresenter extends AbstractMainWithDetailsPresenter<StorageDomain, StorageListModel, MainStoragePresenter.ViewDef, MainStoragePresenter.ProxyDef> {

    @GenEvent
    public class StorageSelectionChange {

        List<StorageDomain> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.storageMainPlace)
    public interface ProxyDef extends ProxyPlace<MainStoragePresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<StorageDomain> {
    }

    @Inject
    public MainStoragePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<StorageDomain, StorageListModel> dataProvider,
            SearchPanelPresenterWidget<StorageDomain, StorageListModel> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<StorageDomain, StorageListModel> breadCrumbs,
            StorageActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, dataProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        StorageSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.storageMainPlace);
    }

}
