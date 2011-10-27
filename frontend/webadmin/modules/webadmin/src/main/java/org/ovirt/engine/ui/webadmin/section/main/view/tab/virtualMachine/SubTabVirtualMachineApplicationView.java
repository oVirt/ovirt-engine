package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineApplicationPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabVirtualMachineApplicationView extends AbstractSubTabTableView<VM, String, VmListModel, VmAppListModel> implements SubTabVirtualMachineApplicationPresenter.ViewDef {

    @Inject
    public SubTabVirtualMachineApplicationView(SearchableDetailModelProvider<String, VmListModel, VmAppListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<String> appNameColumn = new TextColumn<String>() {
            @Override
            public String getValue(String appName) {
                return appName;
            }
        };
        getTable().addColumn(appNameColumn, "Name");
    }
}
