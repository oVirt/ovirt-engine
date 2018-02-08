package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;

public class VirtualMachineSubTabPanelPresenter extends
    AbstractSubTabPanelPresenter<VirtualMachineSubTabPanelPresenter.ViewDef, VirtualMachineSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<VirtualMachineSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    public static final NestedSlot TYPE_SetTabContent = new NestedSlot();

    @Inject
    private MainModelProvider<VM, VmListModel<Void>> modelProvider;

    @Inject
    public VirtualMachineSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            VirtualMachineMainSelectedItems selectedItems) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, selectedItems);
    }

    @Override
    protected void initDetailTabToModelMapping(Map<TabData, Model> mapping) {
        VmListModel<Void> mainModel = modelProvider.getModel();
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_GENERAL, mainModel.getGeneralModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_ERRATA, mainModel.getErrataCountModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_NETWORK_INTERFACE, mainModel.getInterfaceListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_EVENT, mainModel.getEventListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_VIRTUAL_DISK, mainModel.getDiskListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_SNAPSHOT, mainModel.getSnapshotListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_GUEST_CONTAINER, mainModel.getGuestContainerListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_APPLICATION, mainModel.getAppListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_HOST_DEVICE, mainModel.getHostDeviceListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_VM_DEVICE, mainModel.getVmDevicesListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_AFFINITY_GROUP, mainModel.getAffinityGroupListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_AFFINITY_LABEL, mainModel.getAffinityLabelListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_PERMISSION, mainModel.getPermissionListModel());
        mapping.put(DetailTabDataIndex.VIRTUALMACHINE_GUEST_INFO, mainModel.getGuestInfoModel());
    }

}
