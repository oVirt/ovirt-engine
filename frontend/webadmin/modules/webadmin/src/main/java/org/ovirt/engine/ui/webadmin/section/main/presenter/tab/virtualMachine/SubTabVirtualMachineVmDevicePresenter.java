package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.CellClickHandler;
import org.ovirt.engine.ui.common.widget.HasCellClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDeviceFeEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabVirtualMachineVmDevicePresenter
    extends AbstractSubTabVirtualMachinePresenter<VmDevicesListModel<VM>, SubTabVirtualMachineVmDevicePresenter.ViewDef,
        SubTabVirtualMachineVmDevicePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineVmDeviceSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineVmDevicePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
        HasCellClickHandlers<VmDeviceFeEntity> getHotUnplugColumn();
    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.VIRTUALMACHINE_VM_DEVICE;
    }

    @Inject
    public SubTabVirtualMachineVmDevicePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, SearchableDetailModelProvider<VmDeviceFeEntity, VmListModel<Void>, VmDevicesListModel<VM>> modelProvider,
            VirtualMachineMainSelectedItems selectedItems) {
        // View has no action panel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    public void initializeHandlers() {
        super.initializeHandlers();
        registerHandler(getView().getHotUnplugColumn().addHandler(new CellClickHandler<VmDeviceFeEntity>() {
                @Override
                public void onClick(NativeEvent event, VmDeviceFeEntity vmDevice) {
                    getModelProvider().getModel().onHotUnplug(vmDevice);
                }
        }));
    }

}
