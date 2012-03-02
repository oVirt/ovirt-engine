package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.ExtendedVirtualMachineSelectionChangeEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorValueChangeEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorValueChangeEvent.VmMonitorValueChangeHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedVmMonitorPresenter extends AbstractSubTabPresenter<UserPortalItemModel, UserPortalListModel, VmMonitorModel, SubTabExtendedVmMonitorPresenter.ViewDef, SubTabExtendedVmMonitorPresenter.ProxyDef>
        implements VmMonitorValueChangeHandler {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedVirtualMachineMonitorSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedVmMonitorPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {

        void update();

    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new TabDataBasic(ginjector.getApplicationConstants().extendedVirtualMachineMonitorSubTabLabel(), 7);
    }

    @Inject
    public SubTabExtendedVmMonitorPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VmMonitorModelProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, ExtendedVmSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.extendedVirtualMachineSideTabPlace);
    }

    @ProxyEvent
    public void onExtendedVirtualMachineSelectionChange(ExtendedVirtualMachineSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

    @Override
    protected void onDetailModelEntityChange(Object entity) {
        getView().update();
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getEventBus().addHandler(VmMonitorValueChangeEvent.getType(), this));
    }

    @Override
    public void onVmMonitorValueChange(VmMonitorValueChangeEvent event) {
        getView().update();
    }

}
