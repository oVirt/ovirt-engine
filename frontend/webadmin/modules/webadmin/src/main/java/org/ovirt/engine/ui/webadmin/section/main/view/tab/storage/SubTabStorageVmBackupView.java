package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabStorageVmBackupView extends AbstractSubTabTableView<storage_domains, VM, StorageListModel, VmBackupModel>
        implements SubTabStorageVmBackupPresenter.ViewDef {

    @Inject
    public SubTabStorageVmBackupView(SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> modelProvider) {
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

        TextColumn<VM> templateColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvmt_name();
            }
        };
        getTable().addColumn(templateColumn, "Template");

        TextColumn<VM> originColumn = new EnumColumn<VM, OriginType>() {
            @Override
            protected OriginType getRawValue(VM object) {
                return object.getorigin();
            }
        };
        getTable().addColumn(originColumn, "Origin");

        TextColumn<VM> memoryColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getvm_mem_size_mb()) + " MB";
            }
        };
        getTable().addColumn(memoryColumn, "Memory");

        TextColumn<VM> cpuColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getnum_of_cpus());
            }
        };
        getTable().addColumn(cpuColumn, "CPUs");

        TextColumn<VM> diskColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getDiskMap().size());
            }
        };
        getTable().addColumn(diskColumn, "Disks");

        TextColumn<VM> creationDateColumn = new GeneralDateTimeColumn<VM>() {
            @Override
            protected Date getRawValue(VM object) {
                return object.getvm_creation_date();
            }
        };
        getTable().addColumn(creationDateColumn, "Creation Date");
    }

}
