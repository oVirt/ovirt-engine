package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineSelectionChangeEvent;

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

public class SubTabVirtualMachineSnapshotPresenter extends AbstractSubTabPresenter<VM, VmListModel, VmSnapshotListModel, SubTabVirtualMachineSnapshotPresenter.ViewDef, SubTabVirtualMachineSnapshotPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineSnapshotSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineSnapshotPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {

        void addModelListeners();

    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            SearchableDetailModelProvider<Snapshot, VmListModel, VmSnapshotListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.virtualMachineSnapshotSubTabLabel(), 4, modelProvider);
    }

    @Inject
    public SubTabVirtualMachineSnapshotPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<Snapshot, VmListModel, VmSnapshotListModel> modelProvider) {
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

    @Override
    public void initializeHandlers() {
        super.initializeHandlers();

        getView().addModelListeners();
    }

}
