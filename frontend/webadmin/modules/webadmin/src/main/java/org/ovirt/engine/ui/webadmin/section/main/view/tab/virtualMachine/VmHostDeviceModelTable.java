package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import com.google.gwt.event.shared.EventBus;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.HostDeviceModelBaseTable;

public class VmHostDeviceModelTable extends HostDeviceModelBaseTable<VmHostDeviceListModel> {

    public VmHostDeviceModelTable(
            SearchableTableModelProvider<HostDeviceView, VmHostDeviceListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    public void initTable() {
        super.initTable();

        getTable().addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getEventBus(), constants.addVmHostDevice()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getEventBus(), constants.removeVmHostDevice()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getEventBus(), constants.repinVmHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRepinHostCommand();
            }
        });
    }
}
