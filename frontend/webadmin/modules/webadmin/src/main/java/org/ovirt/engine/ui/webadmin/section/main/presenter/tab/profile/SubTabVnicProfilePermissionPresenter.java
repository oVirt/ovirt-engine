package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
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

public class SubTabVnicProfilePermissionPresenter
    extends AbstractSubTabVnicProfilePresenter<PermissionListModel<VnicProfileView>,
        SubTabVnicProfilePermissionPresenter.ViewDef, SubTabVnicProfilePermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.vnicProfilePermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVnicProfilePermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VnicProfileView> {
    }

    @TabInfo(container = VnicProfileSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.VNIC_PROFILE_PERMISSION;
    }

    @Inject
    public SubTabVnicProfilePermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VnicProfileMainSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, VnicProfileListModel, PermissionListModel<VnicProfileView>> modelProvider) {
        // View uses PermissionWithInheritedPermissionListModelTable to get action panel elsewhere passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                VnicProfileSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
