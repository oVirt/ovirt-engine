package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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

public class SubTabDataCenterEventPresenter extends AbstractSubTabPresenter<StoragePool, DataCenterListModel, DataCenterEventListModel, SubTabDataCenterEventPresenter.ViewDef, SubTabDataCenterEventPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterEventSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterEventPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.dataCenterEventSubTabLabel(), 6, modelProvider, Align.RIGHT);
    }

    @Inject
    public SubTabDataCenterEventPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.dataCenterMainTabPlace);
    }

    @ProxyEvent
    public void onDataCenterSelectionChange(DataCenterSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
