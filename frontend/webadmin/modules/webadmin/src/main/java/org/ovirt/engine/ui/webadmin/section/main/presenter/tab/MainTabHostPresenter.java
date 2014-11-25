package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class MainTabHostPresenter extends AbstractMainTabWithDetailsPresenter<VDS, HostListModel, MainTabHostPresenter.ViewDef, MainTabHostPresenter.ProxyDef> {

    @GenEvent
    public class HostSelectionChange {

        List<VDS> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabHostPresenter> {
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            MainModelProvider<VDS, HostListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.hostMainTabLabel(), 2, modelProvider);
    }

    @Inject
    public MainTabHostPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<VDS, HostListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        HostSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.hostMainTabPlace);
    }

}
