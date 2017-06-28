package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.OvirtBreadCrumbs;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.tab.MenuLayoutMenuDetails;
import org.ovirt.engine.ui.webadmin.widget.tab.WebadminMenuLayout;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainTabEventPresenter extends AbstractMainTabWithDetailsPresenter<AuditLog, EventListModel<Void>,
    MainTabEventPresenter.ViewDef, MainTabEventPresenter.ProxyDef> {

    @GenEvent
    public class EventSelectionChange {

        List<AuditLog> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.eventMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabEventPresenter> {
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<AuditLog> {
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(
            MainModelProvider<AuditLog, EventListModel<Void>> modelProvider, WebadminMenuLayout menuLayout) {
        MenuLayoutMenuDetails menuTabDetails =
                menuLayout.getDetails(WebAdminApplicationPlaces.eventMainTabPlace);
        return new ModelBoundTabData(menuTabDetails.getSecondaryTitle(), menuTabDetails.getSecondaryPriority(),
                menuTabDetails.getPrimaryTitle(), menuTabDetails.getPrimaryPriority(), modelProvider,
                menuTabDetails.getIcon());
    }

    @Inject
    public MainTabEventPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<AuditLog, EventListModel<Void>> modelProvider,
            SearchPanelPresenterWidget<AuditLog, EventListModel<Void>> searchPanelPresenterWidget,
            OvirtBreadCrumbs<AuditLog, EventListModel<Void>> breadCrumbs) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.eventMainTabPlace);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        EventSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    public void handlePlaceTransition() {
        // No-op, Event main tab has no sub tabs
    }

}
