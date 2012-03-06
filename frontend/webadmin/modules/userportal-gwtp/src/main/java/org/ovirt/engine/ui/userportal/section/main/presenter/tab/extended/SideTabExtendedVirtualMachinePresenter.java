package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SideTabExtendedVirtualMachinePresenter extends AbstractSideTabWithDetailsPresenter<UserPortalItemModel, UserPortalListModel, SideTabExtendedVirtualMachinePresenter.ViewDef, SideTabExtendedVirtualMachinePresenter.ProxyDef> {

    @GenEvent
    public static class ExtendedVirtualMachineSelectionChange {

        List<UserPortalItemModel> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedVirtualMachineSideTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SideTabExtendedVirtualMachinePresenter> {
    }

    public interface ViewDef extends AbstractSideTabWithDetailsPresenter.ViewDef<UserPortalItemModel> {
    }

    @TabInfo(container = MainTabExtendedPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new TabDataBasic(ginjector.getApplicationConstants().extendedVirtualMachineSideTabLabel(), 0);
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSubTabPanelContent = new Type<RevealContentHandler<?>>();

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
        return new PlaceRequest(ApplicationPlaces.extendedVirtualMachineSideTabPlace);
    }

}
