package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.inject.Inject;

public class SubTabStorageTemplateView extends AbstractSubTabTableView<storage_domains, VmTemplate, StorageListModel, StorageTemplateListModel>
        implements SubTabStorageTemplatePresenter.ViewDef {

    @Inject
    public SubTabStorageTemplateView(SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<VmTemplate> nameColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<VmTemplate> diskColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getDiskMap().size());
            }
        };
        getTable().addColumn(diskColumn, "Disks");

        DiskSizeColumn<VmTemplate> actualSizeColumn = new DiskSizeColumn<VmTemplate>() {
            @Override
            protected Long getRawValue(VmTemplate object) {
                return (long) object.getActualDiskSize();
            }
        };
        getTable().addColumn(actualSizeColumn, "Actual Size");

        TextColumnWithTooltip<VmTemplate> creationDateColumn = new GeneralDateTimeColumn<VmTemplate>() {
            @Override
            protected Date getRawValue(VmTemplate object) {
                return object.getcreation_date();
            }
        };
        getTable().addColumn(creationDateColumn, "Creation Date");
    }

}
