package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.VnicProfileBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VnicProfileActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainVnicProfilePresenter extends AbstractMainWithDetailsPresenter<VnicProfileView, VnicProfileListModel, MainVnicProfilePresenter.ViewDef, MainVnicProfilePresenter.ProxyDef> {

    @GenEvent
    public class VnicProfileSelectionChange {

        List<VnicProfileView> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.vnicProfileMainPlace)
    public interface ProxyDef extends ProxyPlace<MainVnicProfilePresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<VnicProfileView> {
    }

    @Inject
    public MainVnicProfilePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<VnicProfileView, VnicProfileListModel> modelProvider,
            SearchPanelPresenterWidget<VnicProfileView, VnicProfileListModel> searchPanelPresenterWidget,
            VnicProfileBreadCrumbsPresenterWidget breadCrumbs,
            VnicProfileActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        VnicProfileSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.vnicProfileMainPlace);
    }

}
