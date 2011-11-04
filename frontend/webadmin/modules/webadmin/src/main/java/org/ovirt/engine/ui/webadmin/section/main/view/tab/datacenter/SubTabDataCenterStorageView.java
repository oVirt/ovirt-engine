package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabDataCenterStorageView extends AbstractSubTabTableView<storage_pool, storage_domains, DataCenterListModel, DataCenterStorageListModel>
        implements SubTabDataCenterStoragePresenter.ViewDef {

    @Inject
    public SubTabDataCenterStorageView(SearchableDetailModelProvider<storage_domains, DataCenterListModel, DataCenterStorageListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new StorageDomainStatusColumn(), "", "30px");

        TextColumn<storage_domains> nameColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_name();
            }
        };
        getTable().addColumn(nameColumn, "Domain Name");

        TextColumn<storage_domains> typeColumn = new EnumColumn<storage_domains, StorageDomainType>() {
            @Override
            public StorageDomainType getRawValue(storage_domains object) {
                return object.getstorage_domain_type();
            }
        };
        getTable().addColumn(typeColumn, "Domain Type");

        TextColumn<storage_domains> statusColumn = new EnumColumn<storage_domains, StorageDomainSharedStatus>() {
            @Override
            public StorageDomainSharedStatus getRawValue(storage_domains object) {
                return object.getstorage_domain_shared_status();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumn<storage_domains> freeColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getTotalDiskSize() - object.getused_disk_size() + " GB";
            }
        };
        getTable().addColumn(freeColumn, "Free Space");

        TextColumn<storage_domains> usedColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getused_disk_size() + " GB";
            }
        };
        getTable().addColumn(usedColumn, "Used Space");

        TextColumn<storage_domains> totalColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getTotalDiskSize() + " GB";
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
