package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.web.bindery.event.shared.EventBus;

public class VmHostDeviceActionPanelPresenterWidget extends
    ActionPanelPresenterWidget<HostDeviceView, VmHostDeviceListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VmHostDeviceActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<HostDeviceView> view,
            SearchableDetailModelProvider<HostDeviceView, VmListModel<Void>, VmHostDeviceListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getSharedEventBus(), constants.addVmHostDevice()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        });
        addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getSharedEventBus(), constants.removeVmHostDevice()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getSharedEventBus(), constants.repinVmHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRepinHostCommand();
            }
        });
    }

}
