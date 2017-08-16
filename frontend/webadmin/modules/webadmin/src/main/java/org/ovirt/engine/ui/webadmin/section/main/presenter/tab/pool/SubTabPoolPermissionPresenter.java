package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
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

public class SubTabPoolPermissionPresenter
    extends AbstractSubTabPoolPresenter<PermissionListModel<VmPool>, SubTabPoolPermissionPresenter.ViewDef,
        SubTabPoolPermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.poolPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabPoolPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmPool> {
    }

    @TabInfo(container = PoolSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.POOL_PERMISSION;
    }

    @Inject
    public SubTabPoolPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, PoolMainSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, PoolListModel, PermissionListModel<VmPool>> modelProvider) {
        // View uses PermissionWithInheritedPermissionListModelTable to get action panel elsewhere passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                PoolSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
