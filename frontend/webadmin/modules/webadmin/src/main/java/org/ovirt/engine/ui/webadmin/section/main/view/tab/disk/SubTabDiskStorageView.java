package org.ovirt.engine.ui.webadmin.section.main.view.tab.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer.DiskSizeUnit;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskStorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainSharedStatusColumn;

import com.google.gwt.core.client.GWT;

public class SubTabDiskStorageView extends AbstractSubTabTableView<Disk, storage_domains, DiskListModel, DiskStorageListModel>
        implements SubTabDiskStoragePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDiskStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDiskStorageView(SearchableDetailModelProvider<storage_domains, DiskListModel, DiskStorageListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().addColumn(new StorageDomainSharedStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<storage_domains> nameColumn = new TextColumnWithTooltip<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_name();
            }
        };
        getTable().addColumn(nameColumn, constants.domainNameStorage());

        TextColumnWithTooltip<storage_domains> descriptionColumn = new TextColumnWithTooltip<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getDescription();
            }
        };
        getTable().addColumn(descriptionColumn, constants.domainDescriptionStorage());

        TextColumnWithTooltip<storage_domains> typeColumn = new EnumColumn<storage_domains, StorageDomainType>() {
            @Override
            public StorageDomainType getRawValue(storage_domains object) {
                return object.getstorage_domain_type();
            }
        };
        getTable().addColumn(typeColumn, constants.domainTypeStorage());

        TextColumnWithTooltip<storage_domains> statusColumn = new EnumColumn<storage_domains, StorageDomainStatus>() {
            @Override
            public StorageDomainStatus getRawValue(storage_domains object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusStorage());

        DiskSizeColumn<storage_domains> freeColumn = new DiskSizeColumn<storage_domains>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(storage_domains object) {
                long availableDiskSize = object.getavailable_disk_size() != null ? object.getavailable_disk_size() : 0;
                return (long) availableDiskSize;
            }
        };
        getTable().addColumn(freeColumn, constants.freeSpaceStorage());

        DiskSizeColumn<storage_domains> usedColumn = new DiskSizeColumn<storage_domains>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(storage_domains object) {
                long usedDiskSize = object.getused_disk_size() != null ? object.getused_disk_size() : 0;
                return (long) usedDiskSize;
            }
        };
        getTable().addColumn(usedColumn, constants.usedSpaceStorage());

        DiskSizeColumn<storage_domains> totalColumn = new DiskSizeColumn<storage_domains>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(storage_domains object) {
                long totalDiskSize = object.getTotalDiskSize() != null ? object.getTotalDiskSize() : 0;
                return (long) totalDiskSize;
            }
        };
        getTable().addColumn(totalColumn, constants.totalSpaceStorage());
    }

}
