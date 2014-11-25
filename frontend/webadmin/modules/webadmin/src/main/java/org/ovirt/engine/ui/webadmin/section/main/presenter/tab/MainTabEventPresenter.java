package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
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

public class MainTabEventPresenter extends AbstractMainTabWithDetailsPresenter<AuditLog, EventListModel, MainTabEventPresenter.ViewDef, MainTabEventPresenter.ProxyDef> {

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
    static TabData getTabData(ApplicationConstants applicationConstants,
            MainModelProvider<AuditLog, EventListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.eventMainTabLabel(), 14, modelProvider, Align.RIGHT);
    }

    @Inject
    public MainTabEventPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<AuditLog, EventListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setSubTabPanelVisible(false);
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
    protected void handlePlaceTransition() {
        // No-op, Event main tab has no sub tabs
    }

}
