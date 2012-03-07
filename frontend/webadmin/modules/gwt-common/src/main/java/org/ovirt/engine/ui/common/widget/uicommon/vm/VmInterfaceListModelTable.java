package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;

import com.google.gwt.event.shared.EventBus;

public class VmInterfaceListModelTable extends BaseInterfaceListModelTable<VmInterfaceListModel> {

    public VmInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, VmInterfaceListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    public void initTable() {
        super.initTable();

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), "New") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), "Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), "Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
