package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabDataCenterPermissionPresenter extends AbstractSubTabPresenter<StoragePool, DataCenterListModel,
    PermissionListModel<DataCenterListModel>, SubTabDataCenterPermissionPresenter.ViewDef,
    SubTabDataCenterPermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.dataCenterPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<Permissions, DataCenterListModel,
            PermissionListModel<DataCenterListModel>> modelProvider) {
        return new ModelBoundTabData(applicationConstants.dataCenterPermissionSubTabLabel(), 5, modelProvider);
    }

    @Inject
    public SubTabDataCenterPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<Permissions, DataCenterListModel,
            PermissionListModel<DataCenterListModel>> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(ApplicationPlaces.dataCenterMainTabPlace);
    }

    @ProxyEvent
    public void onDataCenterSelectionChange(DataCenterSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
