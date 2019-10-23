package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.AbstractActionTable;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VmHostDeviceActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.HostDeviceModelBaseTable;

import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.shared.EventBus;

public class VmHostDeviceModelTable extends HostDeviceModelBaseTable<VM, VmHostDeviceListModel> implements AbstractActionTable.RowVisitor<HostDeviceView> {

    public VmHostDeviceModelTable(
            SearchableTableModelProvider<HostDeviceView, VmHostDeviceListModel> modelProvider,
            EventBus eventBus, VmHostDeviceActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage);
    }

    @Override
    public void initTable() {
        super.initTable();
        getTable().setRowVisitor(this);
    }

    @Override
    public void visit(TableRowElement row, HostDeviceView item) {
        if (item.isIommuPlaceholder()) {
            row.addClassName("cellTableDisabledRow"); //$NON-NLS-1$
        }
    }
}
