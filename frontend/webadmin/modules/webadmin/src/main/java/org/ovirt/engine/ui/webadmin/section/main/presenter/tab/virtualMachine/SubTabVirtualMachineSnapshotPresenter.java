package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SnapshotActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
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

public class SubTabVirtualMachineSnapshotPresenter
    extends AbstractSubTabVirtualMachinePresenter<VmSnapshotListModel, SubTabVirtualMachineSnapshotPresenter.ViewDef,
        SubTabVirtualMachineSnapshotPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineSnapshotSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineSnapshotPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.VIRTUALMACHINE_SNAPSHOT;
    }

    @Inject
    public SubTabVirtualMachineSnapshotPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VirtualMachineMainSelectedItems selectedItems,
            SnapshotActionPanelPresenterWidget actionPanel,
            SearchableDetailModelProvider<Snapshot, VmListModel<Void>, VmSnapshotListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
