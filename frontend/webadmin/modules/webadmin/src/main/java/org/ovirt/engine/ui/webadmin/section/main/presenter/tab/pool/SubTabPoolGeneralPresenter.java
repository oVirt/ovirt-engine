package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.PoolSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.SubTabPoolGeneralView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabPoolGeneralPresenter extends AbstractSubTabPresenter<VmPool, PoolListModel, PoolGeneralModel, SubTabPoolGeneralView, SubTabPoolGeneralPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.poolGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabPoolGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmPool> {
    }

    @TabInfo(container = PoolSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            DetailModelProvider<PoolListModel, PoolGeneralModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.poolGeneralSubTabLabel(), 0, modelProvider);
    }

    @Inject
    public SubTabPoolGeneralPresenter(EventBus eventBus, SubTabPoolGeneralView view, ProxyDef proxy,
            PlaceManager placeManager, DetailModelProvider<PoolListModel, PoolGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                PoolSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.poolMainTabPlace);
    }

    @ProxyEvent
    public void onPoolSelectionChange(PoolSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
