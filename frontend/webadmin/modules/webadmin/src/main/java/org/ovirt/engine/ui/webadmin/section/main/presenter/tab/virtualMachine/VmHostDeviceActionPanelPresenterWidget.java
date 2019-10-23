package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.web.bindery.event.shared.EventBus;

public class VmHostDeviceActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VM, HostDeviceView, VmListModel<Void>, VmHostDeviceListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VmHostDeviceActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VM, HostDeviceView> view,
            SearchableDetailModelProvider<HostDeviceView, VmListModel<Void>, VmHostDeviceListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<VM, HostDeviceView>(getSharedEventBus(), constants.addVmHostDevice()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddCommand();
            }
        });
        addActionButton(new UiCommandButtonDefinition<VM, HostDeviceView>(getSharedEventBus(), constants.removeVmHostDevice()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
        addActionButton(new UiCommandButtonDefinition<VM, HostDeviceView>(getSharedEventBus(), constants.repinVmHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRepinHostCommand();
            }
        });
    }

}
