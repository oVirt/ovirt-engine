package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import com.google.gwt.event.shared.EventBus;

public class PoolDiskListModelTable extends BaseVmDiskListModelTable<PoolDiskListModel> {

    public PoolDiskListModelTable(
            SearchableTableModelProvider<Disk, PoolDiskListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }
}
