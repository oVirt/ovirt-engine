package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VmHostDeviceActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.HostDeviceModelBaseTable;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.RowStyles;

public class VmHostDeviceModelTable extends HostDeviceModelBaseTable<VM, VmHostDeviceListModel> {

    public VmHostDeviceModelTable(
            SearchableTableModelProvider<HostDeviceView, VmHostDeviceListModel> modelProvider,
            EventBus eventBus,
            VmHostDeviceActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage);
        RowStyles<HostDeviceView> rowStyles =
                (item, rowIndex) -> item.isIommuPlaceholder() ? "cellTableDisabledRow" : null; //$NON-NLS-1$

        getTable().table.setRowStyles(rowStyles);
    }

}
