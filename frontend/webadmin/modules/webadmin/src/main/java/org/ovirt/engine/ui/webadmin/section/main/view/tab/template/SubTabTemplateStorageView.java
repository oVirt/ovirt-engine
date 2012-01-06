package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.renderer.DiskSizeRenderer.DiskSizeUnit;
import org.ovirt.engine.ui.webadmin.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.inject.Inject;

public class SubTabTemplateStorageView extends AbstractSubTabTableView<VmTemplate, storage_domains, TemplateListModel, TemplateStorageListModel>
        implements SubTabTemplateStoragePresenter.ViewDef {

    @Inject
    public SubTabTemplateStorageView(SearchableDetailModelProvider<storage_domains, TemplateListModel, TemplateStorageListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new StorageDomainStatusColumn(), "", "30px");

        TextColumnWithTooltip<storage_domains> nameColumn = new TextColumnWithTooltip<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_name();
            }
        };
        getTable().addColumn(nameColumn, "Domain Name");

        TextColumnWithTooltip<storage_domains> typeColumn = new EnumColumn<storage_domains, StorageDomainType>() {
            @Override
            protected StorageDomainType getRawValue(storage_domains object) {
                return object.getstorage_domain_type();
            }
        };
        getTable().addColumn(typeColumn, "Domain Type");

        TextColumnWithTooltip<storage_domains> statusColumn = new EnumColumn<storage_domains, StorageDomainStatus>() {
            @Override
            protected StorageDomainStatus getRawValue(storage_domains object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        DiskSizeColumn<storage_domains> freeColumn = new DiskSizeColumn<storage_domains>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(storage_domains object) {
                long availableDiskSize = object.getavailable_disk_size() != null ? object.getavailable_disk_size() : 0;
                return (long) availableDiskSize;
            }
        };
        getTable().addColumn(freeColumn, "Free Space");

        DiskSizeColumn<storage_domains> usedColumn = new DiskSizeColumn<storage_domains>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(storage_domains object) {
                long usedDiskSize = object.getused_disk_size() != null ? object.getused_disk_size() : 0;
                return (long) usedDiskSize;
            }
        };
        getTable().addColumn(usedColumn, "Used Space");

        DiskSizeColumn<storage_domains> totalColumn = new DiskSizeColumn<storage_domains>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(storage_domains object) {
                long totalDiskSize = object.getTotalDiskSize() != null ? object.getTotalDiskSize() : 0;
                return (long) totalDiskSize;
            }
        };
        getTable().addColumn(totalColumn, "Total Space");
    }

}
