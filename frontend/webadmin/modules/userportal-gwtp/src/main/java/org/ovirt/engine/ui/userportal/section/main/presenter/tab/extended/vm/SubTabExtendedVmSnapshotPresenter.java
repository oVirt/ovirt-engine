package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmSnapshotListModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSnapshotListModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedVmSnapshotPresenter
        extends AbstractSubTabExtendedVmPresenter<UserPortalVmSnapshotListModel, SubTabExtendedVmSnapshotPresenter.ViewDef, SubTabExtendedVmSnapshotPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedVirtualMachineSnapshotSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedVmSnapshotPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {
    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            VmSnapshotListModelProvider modelProvider) {
        return new ModelBoundTabData(applicationConstants.extendedVirtualMachineSnapshotSubTabLabel(), 6, modelProvider);
    }

    @Inject
    public SubTabExtendedVmSnapshotPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VmSnapshotListModelProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

}
