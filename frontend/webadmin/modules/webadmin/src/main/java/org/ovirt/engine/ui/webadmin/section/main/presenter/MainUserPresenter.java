package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter.UserOrGroup;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.UserActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainUserPresenter extends AbstractMainWithDetailsPresenter<DbUser, UserListModel, MainUserPresenter.ViewDef, MainUserPresenter.ProxyDef> {

    @GenEvent
    public class UserSelectionChange {

        List<DbUser> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.userMainPlace)
    public interface ProxyDef extends ProxyPlace<MainUserPresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<DbUser> {
        void userTypeChanged(UserOrGroup newType);
    }

    @Inject
    public MainUserPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<DbUser, UserListModel> modelProvider,
            SearchPanelPresenterWidget<DbUser, UserListModel> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<DbUser, UserListModel> breadCrumbs,
            UserActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().userTypeChanged(UserOrGroup.User);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        UserSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.userMainPlace);
    }

}
