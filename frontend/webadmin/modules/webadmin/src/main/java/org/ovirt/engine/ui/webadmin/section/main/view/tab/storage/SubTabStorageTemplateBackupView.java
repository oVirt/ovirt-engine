package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplateBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;

import com.google.inject.Inject;

public class SubTabStorageTemplateBackupView extends AbstractSubTabTableView<storage_domains, VmTemplate, StorageListModel, TemplateBackupModel>
        implements SubTabStorageTemplateBackupPresenter.ViewDef {

    @Inject
    public SubTabStorageTemplateBackupView(SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<VmTemplate> nameColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getname();
                    }
                };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<VmTemplate> originColumn =
                new EnumColumn<VmTemplate, OriginType>() {
                    @Override
                    protected OriginType getRawValue(VmTemplate object) {
                        return object.getorigin();
                    }
                };
        getTable().addColumn(originColumn, "Origin");

        TextColumnWithTooltip<VmTemplate> memoryColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getmem_size_mb()) + " MB";
                    }
                };
        getTable().addColumn(memoryColumn, "Memory");

        TextColumnWithTooltip<VmTemplate> cpuColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getnum_of_cpus());
                    }
                };
        getTable().addColumn(cpuColumn, "CPUs");

        TextColumnWithTooltip<VmTemplate> diskColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getDiskList().size());
                    }
                };
        getTable().addColumn(diskColumn, "Disks");

        TextColumnWithTooltip<VmTemplate> creationDateColumn =
                new GeneralDateTimeColumn<VmTemplate>() {
                    @Override
                    protected Date getRawValue(VmTemplate object) {
                        return object.getcreation_date();
                    }
                };
        getTable().addColumn(creationDateColumn, "Creation Date");

        getTable().addActionButton(new WebAdminButtonDefinition<VmTemplate>("Restore") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRestoreCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<VmTemplate>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
