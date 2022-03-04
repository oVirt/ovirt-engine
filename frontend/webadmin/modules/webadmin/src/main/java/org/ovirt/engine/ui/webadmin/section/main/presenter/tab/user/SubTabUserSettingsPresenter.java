package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserSettingsModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserSettingsView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabUserSettingsPresenter
        extends AbstractSubTabUserPresenter<UserSettingsModel, SubTabUserSettingsPresenter.ViewDef,
        SubTabUserSettingsPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.userSettingsSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabUserSettingsPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<DbUser> {
    }

    @TabInfo(container = UserSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.USER_SETTINGS;
    }

    @Inject
    public SubTabUserSettingsPresenter(EventBus eventBus, SubTabUserSettingsView view, SubTabUserSettingsPresenter.ProxyDef proxy,
            PlaceManager placeManager, UserMainSelectedItems selectedItems,
            SearchableDetailModelProvider<UserProfileProperty, UserListModel, UserSettingsModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                UserSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
