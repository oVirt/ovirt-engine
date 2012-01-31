package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
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

public class MainTabPoolPresenter extends AbstractMainTabWithDetailsPresenter<vm_pools, PoolListModel, MainTabPoolPresenter.ViewDef, MainTabPoolPresenter.ProxyDef> {

    @GenEvent
    public static class PoolSelectionChange {

        List<vm_pools> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.poolMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabPoolPresenter> {
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<vm_pools> {
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().poolMainTabLabel(), 5,
                ginjector.getMainTabPoolModelProvider());
    }

    @Inject
    public MainTabPoolPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<vm_pools, PoolListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        PoolSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.poolMainTabPlace);
    }
}
