package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
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

public class SubTabVirtualMachineGuestInfoPresenter
    extends AbstractSubTabVirtualMachinePresenter<VmGuestInfoModel, SubTabVirtualMachineGuestInfoPresenter.ViewDef,
        SubTabVirtualMachineGuestInfoPresenter.ProxyDef> {

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineGuestInfoSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineGuestInfoPresenter> {

    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.VIRTUALMACHINE_GUEST_INFO;
    }

    @Inject
    public SubTabVirtualMachineGuestInfoPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VirtualMachineMainSelectedItems selectedItems,
            DetailModelProvider<VmListModel<Void>, VmGuestInfoModel> modelProvider) {
        // View has no action panel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
