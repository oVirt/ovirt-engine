package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolVmListModel;
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

public class SubTabPoolVmPresenter
    extends AbstractSubTabPoolPresenter<PoolVmListModel, SubTabPoolVmPresenter.ViewDef,
        SubTabPoolVmPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.poolVmSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabPoolVmPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmPool> {
    }

    @TabInfo(container = PoolSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<VM, PoolListModel, PoolVmListModel> modelProvider) {
        return new ModelBoundTabData(constants.poolVmSubTabLabel(), 1, modelProvider);
    }

    @Inject
    public SubTabPoolVmPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, PoolMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<VM, PoolListModel, PoolVmListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                PoolSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
