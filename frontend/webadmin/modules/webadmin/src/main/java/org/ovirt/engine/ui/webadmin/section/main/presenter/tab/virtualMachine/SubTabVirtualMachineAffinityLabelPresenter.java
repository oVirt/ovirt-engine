package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.VmAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.AffinityLabelsActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabVirtualMachineAffinityLabelPresenter
        extends AbstractSubTabVirtualMachinePresenter<VmAffinityLabelListModel,
        SubTabVirtualMachineAffinityLabelPresenter.ViewDef, SubTabVirtualMachineAffinityLabelPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineAffinityLabelsSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineAffinityLabelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData() {
        return new GroupedTabData(constants.affinityLabelsSubTabLabel(), 8);
    }

    @Inject
    public SubTabVirtualMachineAffinityLabelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VirtualMachineMainTabSelectedItems selectedItems,
            AffinityLabelsActionPanelPresenterWidget<VmListModel<Void>, VmAffinityLabelListModel> actionPanel,
            SearchableDetailModelProvider<Label, VmListModel<Void>, VmAffinityLabelListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
