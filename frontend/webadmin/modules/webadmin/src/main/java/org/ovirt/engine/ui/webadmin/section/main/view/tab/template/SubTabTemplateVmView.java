package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.UptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabTemplateVmView extends AbstractSubTabTableView<VmTemplate, VM, TemplateListModel, TemplateVmListModel>
        implements SubTabTemplateVmPresenter.ViewDef {

    @Inject
    public SubTabTemplateVmView(SearchableDetailModelProvider<VM, TemplateListModel, TemplateVmListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new VmStatusColumn(), "", "30px");

        TextColumn<VM> nameColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        getTable().addColumn(new VmTypeColumn(), "", "30px");

        TextColumn<VM> hostColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getrun_on_vds_name();
            }
        };
        getTable().addColumn(hostColumn, "Host");

        TextColumn<VM> ipColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_ip();
            }
        };
        getTable().addColumn(ipColumn, "IP Address");

        TextColumn<VM> statusColumn = new EnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumn<VM> uptimeColumn = new UptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        getTable().addColumn(uptimeColumn, "Uptime");

        TextColumn<VM> loggedInUserColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getguest_cur_user_name();
            }
        };
        getTable().addColumn(loggedInUserColumn, "Logged-in User");
    }

}
