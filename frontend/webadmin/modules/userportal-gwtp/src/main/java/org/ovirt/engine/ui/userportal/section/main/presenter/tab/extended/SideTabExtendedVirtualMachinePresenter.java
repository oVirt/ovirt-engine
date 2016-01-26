package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended;

import java.util.List;

import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.AbstractSideTabWithDetailsPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.UserPortalListProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class SideTabExtendedVirtualMachinePresenter extends AbstractSideTabWithDetailsPresenter<UserPortalItemModel, UserPortalListModel, SideTabExtendedVirtualMachinePresenter.ViewDef, SideTabExtendedVirtualMachinePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @GenEvent
    public class ExtendedVirtualMachineSelectionChange {

        List<UserPortalItemModel> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedVirtualMachineSideTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SideTabExtendedVirtualMachinePresenter> {
    }

    public interface ViewDef extends AbstractSideTabWithDetailsPresenter.ViewDef<UserPortalItemModel> {
    }

    @TabInfo(container = MainTabExtendedPresenter.class)
    static TabData getTabData() {
        return new TabDataBasic(constants.extendedVirtualMachineSideTabLabel(), 0);
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSubTabPanelContent = new Type<>();

    @Inject
    public SideTabExtendedVirtualMachinePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, UserPortalListProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        ExtendedVirtualMachineSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getSideTabRequest() {
        return PlaceRequestFactory.get(UserPortalApplicationPlaces.extendedVirtualMachineSideTabPlace);
    }

    /**
     * This method is a hack which enables to have pool and VM subtabs to be bound with the same title
     */
    @Override
    protected String createRequestToken() {
        String requestToken = super.createRequestToken();
        HasEntity<?> model = modelProvider.getModel().getActiveDetailModel();
        if (model instanceof PoolGeneralModel ||
                model instanceof PoolInterfaceListModel ||
                model instanceof PoolDiskListModel
        ) {
            requestToken += UserPortalApplicationPlaces.POOL_SUFFIX;
        }
        return requestToken;
    }

}
