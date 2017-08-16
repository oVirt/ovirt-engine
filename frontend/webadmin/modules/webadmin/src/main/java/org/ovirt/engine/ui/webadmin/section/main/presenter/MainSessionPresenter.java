package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SessionListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.SessionActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainSessionPresenter extends AbstractMainWithDetailsPresenter<UserSession,
    SessionListModel, MainSessionPresenter.ViewDef, MainSessionPresenter.ProxyDef> {

    @GenEvent
    public class SessionSelectionChange {

        List<UserSession> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.sessionMainPlace)
    public interface ProxyDef extends ProxyPlace<MainSessionPresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<UserSession> {
    }

    @Inject
    public MainSessionPresenter(EventBus eventBus,
            ViewDef view,
            ProxyDef proxy,
            PlaceManager placeManager,
            MainModelProvider<UserSession, SessionListModel> modelProvider,
            SearchPanelPresenterWidget<UserSession, SessionListModel> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<UserSession, SessionListModel> breadCrumbs,
            SessionActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        SessionSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.sessionMainPlace);
    }

    @Override
    protected boolean hasSelectionDetails() {
        return false;
    }

}

