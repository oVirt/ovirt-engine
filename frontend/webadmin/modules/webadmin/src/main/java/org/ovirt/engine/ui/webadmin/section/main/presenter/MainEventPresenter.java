package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.EventActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainEventPresenter extends AbstractMainWithDetailsPresenter<AuditLog, EventListModel<Void>,
    MainEventPresenter.ViewDef, MainEventPresenter.ProxyDef> {

    @GenEvent
    public class EventSelectionChange {

        List<AuditLog> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.eventMainPlace)
    public interface ProxyDef extends ProxyPlace<MainEventPresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<AuditLog> {
    }

    @Inject
    public MainEventPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<AuditLog, EventListModel<Void>> modelProvider,
            SearchPanelPresenterWidget<AuditLog, EventListModel<Void>> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<AuditLog, EventListModel<Void>> breadCrumbs,
            EventActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.eventMainPlace);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        EventSelectionChangeEvent.fire(this, getSelectedItems());
    }
}
