package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabUserPermissionPresenter
    extends AbstractSubTabUserPresenter<UserPermissionListModel, SubTabUserPermissionPresenter.ViewDef,
        SubTabUserPermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.userPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabUserPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<DbUser> {
    }

    @TabInfo(container = UserSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.USER_PERMISSION;
    }

    @Inject
    public SubTabUserPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, UserMainSelectedItems selectedItems,
            UserPermissionActionPanelPresenterWidget actionPanel,
            SearchableDetailModelProvider<Permission, UserListModel, UserPermissionListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                UserSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
