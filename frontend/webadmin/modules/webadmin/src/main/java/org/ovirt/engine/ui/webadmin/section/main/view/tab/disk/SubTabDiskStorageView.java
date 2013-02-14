package org.ovirt.engine.ui.webadmin.section.main.view.tab.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
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

public class SubTabDiskStorageView extends AbstractSubTabTableView<Disk, StorageDomain, DiskListModel, DiskStorageListModel>
        implements SubTabDiskStoragePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDiskStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDiskStorageView(SearchableDetailModelProvider<StorageDomain, DiskListModel, DiskStorageListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().addColumn(new StorageDomainSharedStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<StorageDomain> nameColumn = new TextColumnWithTooltip<StorageDomain>() {
            @Override
            public String getValue(StorageDomain object) {
                return object.getstorage_name();
            }
        };
        getTable().addColumn(nameColumn, constants.domainNameStorage());

        TextColumnWithTooltip<StorageDomain> descriptionColumn = new TextColumnWithTooltip<StorageDomain>() {
            @Override
            public String getValue(StorageDomain object) {
                return object.getDescription();
            }
        };
        getTable().addColumn(descriptionColumn, constants.domainDescriptionStorage());

        TextColumnWithTooltip<StorageDomain> typeColumn = new EnumColumn<StorageDomain, StorageDomainType>() {
            @Override
            public StorageDomainType getRawValue(StorageDomain object) {
                return object.getstorage_domain_type();
            }
        };
        getTable().addColumn(typeColumn, constants.domainTypeStorage());

        TextColumnWithTooltip<StorageDomain> statusColumn = new EnumColumn<StorageDomain, StorageDomainStatus>() {
            @Override
            public StorageDomainStatus getRawValue(StorageDomain object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusStorage());

        DiskSizeColumn<StorageDomain> freeColumn = new DiskSizeColumn<StorageDomain>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(StorageDomain object) {
                long availableDiskSize = object.getavailable_disk_size() != null ? object.getavailable_disk_size() : 0;
                return (long) availableDiskSize;
            }
        };
        getTable().addColumn(freeColumn, constants.freeSpaceStorage());

        DiskSizeColumn<StorageDomain> usedColumn = new DiskSizeColumn<StorageDomain>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(StorageDomain object) {
                long usedDiskSize = object.getused_disk_size() != null ? object.getused_disk_size() : 0;
                return (long) usedDiskSize;
            }
        };
        getTable().addColumn(usedColumn, constants.usedSpaceStorage());

        DiskSizeColumn<StorageDomain> totalColumn = new DiskSizeColumn<StorageDomain>(DiskSizeUnit.GIGABYTE) {
            @Override
            public Long getRawValue(StorageDomain object) {
                long totalDiskSize = object.getTotalDiskSize() != null ? object.getTotalDiskSize() : 0;
                return (long) totalDiskSize;
            }
        };
        getTable().addColumn(totalColumn, constants.totalSpaceStorage());
    }

}
