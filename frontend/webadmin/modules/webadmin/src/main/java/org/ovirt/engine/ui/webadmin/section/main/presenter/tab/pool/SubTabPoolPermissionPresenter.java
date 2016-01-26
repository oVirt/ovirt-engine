package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
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

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.poolPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabPoolPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmPool> {
    }

    @TabInfo(container = PoolSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<Permission, PoolListModel,
            PermissionListModel<VmPool>> modelProvider) {
        return new ModelBoundTabData(constants.poolPermissionSubTabLabel(), 2, modelProvider);
    }

    @Inject
    public SubTabPoolPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, PoolMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, PoolListModel, PermissionListModel<VmPool>> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                PoolSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
