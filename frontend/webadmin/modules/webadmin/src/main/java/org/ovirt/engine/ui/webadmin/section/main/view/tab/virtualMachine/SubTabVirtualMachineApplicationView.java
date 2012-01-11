package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineApplicationPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.inject.Inject;

public class SubTabVirtualMachineApplicationView extends AbstractSubTabTableView<VM, String, VmListModel, VmAppListModel> implements SubTabVirtualMachineApplicationPresenter.ViewDef {

    @Inject
    public SubTabVirtualMachineApplicationView(SearchableDetailModelProvider<String, VmListModel, VmAppListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<String> appNameColumn = new TextColumnWithTooltip<String>() {
            @Override
            public String getValue(String appName) {
                return appName;
            }
        };
        getTable().addColumn(appNameColumn, "Installed Applications");
    }

}
