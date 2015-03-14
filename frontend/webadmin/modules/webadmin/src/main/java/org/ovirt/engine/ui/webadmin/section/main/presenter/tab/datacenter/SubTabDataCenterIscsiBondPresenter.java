package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

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

import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterIscsiBondListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterSelectionChangeEvent;

public class SubTabDataCenterIscsiBondPresenter extends AbstractSubTabPresenter<StoragePool, DataCenterListModel,
        DataCenterIscsiBondListModel, SubTabDataCenterIscsiBondPresenter.ViewDef, SubTabDataCenterIscsiBondPresenter.ProxyDef> {

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterIscsiBondSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterIscsiBondPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<StoragePool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData(
                              SearchableDetailModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel> modelProvider) {
        return new ModelBoundTabData(constants.dataCenterIscsiMultipathingSubTabLabel(), 1, modelProvider);
    }

    @Inject
    public SubTabDataCenterIscsiBondPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
        PlaceManager placeManager, SearchableDetailModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
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
