package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.VmInterfaceActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
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

public class SubTabVirtualMachineNetworkInterfacePresenter
    extends AbstractSubTabVirtualMachinePresenter<VmInterfaceListModel,
        SubTabVirtualMachineNetworkInterfacePresenter.ViewDef, SubTabVirtualMachineNetworkInterfacePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineNetworkInterfaceSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineNetworkInterfacePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.VIRTUALMACHINE_NETWORK_INTERFACE;
    }

    @Inject
    public SubTabVirtualMachineNetworkInterfacePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VirtualMachineMainSelectedItems selectedItems,
            VmInterfaceActionPanelPresenterWidget actionPanel,
            SearchableDetailModelProvider<VmNetworkInterface, VmListModel<Void>, VmInterfaceListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
