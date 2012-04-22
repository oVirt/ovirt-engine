package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;

import com.google.gwt.event.shared.EventBus;

public class VmInterfaceListModelTable extends BaseInterfaceListModelTable<VmInterfaceListModel> {

    public VmInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, VmInterfaceListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, CommonApplicationTemplates templates) {
        super(modelProvider, eventBus, clientStorage, templates);
    }

    @Override
    public void initTable(CommonApplicationConstants constants) {
        super.initTable(constants);

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), constants.newInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), constants.editInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getEventBus(), constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
