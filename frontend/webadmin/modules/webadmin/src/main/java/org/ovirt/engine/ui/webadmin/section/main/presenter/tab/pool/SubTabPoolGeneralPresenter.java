package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.SubTabPoolGeneralView;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabPoolGeneralPresenter
    extends AbstractSubTabPoolPresenter<PoolGeneralModel, SubTabPoolGeneralView, SubTabPoolGeneralPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.poolGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabPoolGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmPool> {
    }

    @TabInfo(container = PoolSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<PoolListModel, PoolGeneralModel> modelProvider) {
        return new ModelBoundTabData(constants.poolGeneralSubTabLabel(), 0, modelProvider);
    }

    @Inject
    public SubTabPoolGeneralPresenter(EventBus eventBus, SubTabPoolGeneralView view, ProxyDef proxy,
            PlaceManager placeManager, PoolMainTabSelectedItems selectedItems,
            DetailModelProvider<PoolListModel, PoolGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                PoolSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
