package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;

import com.google.gwt.event.shared.EventBus;

public class PoolInterfaceListModelTable extends BaseInterfaceListModelTable<PoolInterfaceListModel> {

    public PoolInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, PoolInterfaceListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, CommonApplicationTemplates templates) {
        super(modelProvider, eventBus, clientStorage, templates);
    }
}
