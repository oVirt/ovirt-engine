package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
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

public class SubTabVirtualMachineGuestInfoPresenter
    extends AbstractSubTabVirtualMachinePresenter<VmGuestInfoModel, SubTabVirtualMachineGuestInfoPresenter.ViewDef,
        SubTabVirtualMachineGuestInfoPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineGuestInfoSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineGuestInfoPresenter> {

    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<VmListModel<Void>, VmGuestInfoModel> modelProvider) {
        return new ModelBoundTabData(constants.virtualMachineGuestInfoSubTabLabel(), 8, modelProvider);
    }

    @Inject
    public SubTabVirtualMachineGuestInfoPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VirtualMachineMainTabSelectedItems selectedItems,
            DetailModelProvider<VmListModel<Void>, VmGuestInfoModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
