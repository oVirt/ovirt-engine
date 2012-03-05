package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSnapshotListModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedVmSnapshotPresenter
        extends AbstractSubTabExtendedVmPresenter<VmSnapshotListModel, SubTabExtendedVmSnapshotPresenter.ViewDef, SubTabExtendedVmSnapshotPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedVirtualMachineSnapshotSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedVmSnapshotPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {
    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new TabDataBasic(ginjector.getApplicationConstants().extendedVirtualMachineSnapshotSubTabLabel(),
                3);
    }

    @Inject
    public SubTabExtendedVmSnapshotPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VmSnapshotListModelProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

}
