package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachinePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstrctSubTabPermissionsView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.PermissionTypeColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabVirtualMachinePermissionView extends AbstrctSubTabPermissionsView<VM, VmListModel>
        implements SubTabVirtualMachinePermissionPresenter.ViewDef {

    @Inject
    public SubTabVirtualMachinePermissionView(SearchableDetailModelProvider<permissions, VmListModel, PermissionListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

    @Override
    protected void initTable() {
        getTable().addColumn(new PermissionTypeColumn(), "", "30px");

        TextColumn<permissions> userColumn = new TextColumn<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getOwnerName();
            }
        };
        getTable().addColumn(userColumn, "User");

        TextColumn<permissions> roleColumn = new TextColumn<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getRoleName();
            }
        };
        getTable().addColumn(roleColumn, "Role");
        
        getTable().addActionButton(new UiCommandButtonDefinition<permissions>(getDetailModel().getAddCommand(), "Add"));
        getTable().addActionButton(new UiCommandButtonDefinition<permissions>(getDetailModel().getRemoveCommand(),
                "Remove"));
    }

}
