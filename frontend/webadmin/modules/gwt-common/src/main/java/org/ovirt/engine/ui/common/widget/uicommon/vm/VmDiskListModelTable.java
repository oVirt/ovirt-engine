package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.VmDiskActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;

import com.google.gwt.event.shared.EventBus;

public class VmDiskListModelTable extends BaseVmDiskListModelTable<VM, VmDiskListModel> {

    public VmDiskListModelTable(
            SearchableTableModelProvider<Disk, VmDiskListModel> modelProvider,
            EventBus eventBus, VmDiskActionPanelPresenterWidget actionPanel, ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage);
    }
}
