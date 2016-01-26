package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorValueChangeEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorValueChangeEvent.VmMonitorValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedVmMonitorPresenter
        extends AbstractSubTabExtendedVmPresenter<VmMonitorModel, SubTabExtendedVmMonitorPresenter.ViewDef,
            SubTabExtendedVmMonitorPresenter.ProxyDef>
        implements VmMonitorValueChangeHandler {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedVirtualMachineMonitorSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedVmMonitorPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {

        void update();

    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(
            VmMonitorModelProvider modelProvider) {
        return new ModelBoundTabData(constants.extendedVirtualMachineMonitorSubTabLabel(), 10, modelProvider);
    }

    @Inject
    public SubTabExtendedVmMonitorPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ExtendedVmMainTabSelectedItems selectedItems,
            VmMonitorModelProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, selectedItems, modelProvider);
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
