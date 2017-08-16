package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainDataCenterPresenter extends AbstractMainWithDetailsPresenter<StoragePool,
    DataCenterListModel, MainDataCenterPresenter.ViewDef, MainDataCenterPresenter.ProxyDef> {

    @GenEvent
    public class DataCenterSelectionChange {

        List<StoragePool> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterMainPlace)
    public interface ProxyDef extends ProxyPlace<MainDataCenterPresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<StoragePool> {
    }

    @Inject
    public MainDataCenterPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<StoragePool, DataCenterListModel> modelProvider,
            SearchPanelPresenterWidget<StoragePool, DataCenterListModel> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<StoragePool, DataCenterListModel> breadCrumbs,
            DataCenterActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        DataCenterSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.dataCenterMainPlace);
    }

}
