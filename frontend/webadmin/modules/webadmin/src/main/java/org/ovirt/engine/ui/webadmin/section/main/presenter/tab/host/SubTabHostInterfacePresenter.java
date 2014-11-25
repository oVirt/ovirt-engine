package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.refresh.ManualRefreshEvent;
import org.ovirt.engine.ui.common.widget.refresh.ManualRefreshEvent.ManualRefreshHandler;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.GridTimerStateChangeEvent;
import org.ovirt.engine.ui.uicommonweb.models.GridTimerStateChangeEvent.GridTimerStateChangeEventHandler;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent;

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

public class SubTabHostInterfacePresenter extends AbstractSubTabPresenter<VDS, HostListModel, HostInterfaceListModel,
        SubTabHostInterfacePresenter.ViewDef, SubTabHostInterfacePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostInterfaceSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostInterfacePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
        void removeContent();
        void setRefreshButtonVisibility(boolean visible);
        void setParentOverflow();
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel, HostInterfaceListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.hostIfaceSubTabLabel(), 2, modelProvider);
    }

    @Inject
    public SubTabHostInterfacePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel, HostInterfaceListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                HostSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.hostMainTabPlace);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(ManualRefreshEvent.getType(), new ManualRefreshHandler() {
            @Override
            public void onManualRefresh(ManualRefreshEvent event) {
                if (SubTabHostInterfacePresenter.this.isVisible()
                        && SubTabHostInterfacePresenter.this.getModelProvider().getModel().equals(event.getSource())) {
                    getView().removeContent();
                }
            }
        }));
        getModelProvider().getModel().getTimer().addGridTimerStateChangeEventHandler(new GridTimerStateChangeEventHandler() {
            @Override
            public void onGridTimerStateChange(GridTimerStateChangeEvent event) {
                getView().setRefreshButtonVisibility(!getModelProvider().getModel().getTimer().isActive());
            }
        });
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().setParentOverflow();
    }

    @ProxyEvent
    public void onHostSelectionChange(HostSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }
}
