package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineSelectionChangeEvent;

public class SubTabVirtualMachineVmDevicePresenter extends AbstractSubTabPresenter<VM, VmListModel<Void>,
        VmDevicesListModel<VM>, SubTabVirtualMachineVmDevicePresenter.ViewDef,
        SubTabVirtualMachineVmDevicePresenter.ProxyDef> {

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineVmDeviceSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineVmDevicePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<VmDevice, VmListModel<Void>, VmDevicesListModel<VM>> modelProvider) {
        return new ModelBoundTabData(constants.virtualMachineVmDevicesSubTabLabel(), 6, modelProvider);
    }

    @Inject
    public SubTabVirtualMachineVmDevicePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, SearchableDetailModelProvider<VmDevice, VmListModel<Void>, VmDevicesListModel<VM>> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.virtualMachineMainTabPlace);
    }

    @ProxyEvent
    public void onVirtualMachineSelectionChange(VirtualMachineSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }
}
