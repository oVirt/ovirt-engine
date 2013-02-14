package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class MainTabStoragePresenter extends AbstractMainTabWithDetailsPresenter<StorageDomain, StorageListModel, MainTabStoragePresenter.ViewDef, MainTabStoragePresenter.ProxyDef> {

    @GenEvent
    public static class StorageSelectionChange {

        List<StorageDomain> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.storageMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabStoragePresenter> {
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<StorageDomain> {
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().storageMainTabLabel(), 4,
                ginjector.getMainTabStorageModelProvider());
    }

    @Inject
    public MainTabStoragePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<StorageDomain, StorageListModel> dataProvider) {
        super(eventBus, view, proxy, placeManager, dataProvider);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        StorageSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.storageMainTabPlace);
    }

}
