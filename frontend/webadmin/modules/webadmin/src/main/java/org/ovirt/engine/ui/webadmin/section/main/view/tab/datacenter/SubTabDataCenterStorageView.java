package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;

public class SubTabDataCenterStorageView extends AbstractSubTabTableView<storage_pool, storage_domains, DataCenterListModel, DataCenterStorageListModel>
        implements SubTabDataCenterStoragePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDataCenterStorageView(SearchableDetailModelProvider<storage_domains, DataCenterListModel, DataCenterStorageListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
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
            public StorageDomainType getRawValue(storage_domains object) {
                return object.getstorage_domain_type();
            }
        };
        getTable().addColumn(typeColumn, "Domain Type");

        TextColumnWithTooltip<storage_domains> statusColumn = new EnumColumn<storage_domains, StorageDomainStatus>() {
            @Override
            public StorageDomainStatus getRawValue(storage_domains object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        DiskSizeColumn<storage_domains> freeColumn = new DiskSizeColumn<storage_domains>() {
            @Override
            public Long getRawValue(storage_domains object) {
                long availableDiskSize = object.getavailable_disk_size() != null ? object.getavailable_disk_size() : 0;
                return (long) (availableDiskSize * Math.pow(1024, 3));
            }
        };
        getTable().addColumn(freeColumn, "Free Space");

        DiskSizeColumn<storage_domains> usedColumn = new DiskSizeColumn<storage_domains>() {
            @Override
            public Long getRawValue(storage_domains object) {
                long usedDiskSize = object.getused_disk_size() != null ? object.getused_disk_size() : 0;
                return (long) (usedDiskSize * Math.pow(1024, 3));
            }
        };
        getTable().addColumn(usedColumn, "Used Space");

        DiskSizeColumn<storage_domains> totalColumn = new DiskSizeColumn<storage_domains>() {
            @Override
            public Long getRawValue(storage_domains object) {
                long totalDiskSize = object.getTotalDiskSize() != null ? object.getTotalDiskSize() : 0;
                return (long) (totalDiskSize * Math.pow(1024, 3));
            }
        };
        getTable().addColumn(totalColumn, "Total Space");

        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Attach Data") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachStorageCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Attach ISO") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachISOCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Attach Export") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAttachBackupCommand();
            }
        });
        // TODO: Separator
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Detach") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
        // TODO: Separator
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Activate") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getActivateCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>("Maintenance") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getMaintenanceCommand();
            }
        });
    }

}
