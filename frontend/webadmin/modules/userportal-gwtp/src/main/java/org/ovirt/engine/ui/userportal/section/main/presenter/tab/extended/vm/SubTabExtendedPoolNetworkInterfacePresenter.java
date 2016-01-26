package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedPoolNetworkInterfacePresenter
        extends AbstractSubTabExtendedVmPresenter<PoolInterfaceListModel,
            SubTabExtendedPoolNetworkInterfacePresenter.ViewDef, SubTabExtendedPoolNetworkInterfacePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedPoolNetworkInterfaceSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedPoolNetworkInterfacePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {
    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(
            UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel,
                PoolInterfaceListModel> modelProvider) {
        return new ModelBoundTabData(constants.extendedVirtualMachineNetworkInterfaceSubTabLabel(), 3, modelProvider);
    }

    @Inject
    public SubTabExtendedPoolNetworkInterfacePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ExtendedVmMainTabSelectedItems selectedItems,
            UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel,
                PoolInterfaceListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, selectedItems, modelProvider);
    }

}
