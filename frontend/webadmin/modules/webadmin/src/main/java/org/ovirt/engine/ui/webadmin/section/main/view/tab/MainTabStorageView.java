package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainSharedStatusColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class MainTabStorageView extends AbstractMainTabWithDetailsTableView<storage_domains, StorageListModel> implements MainTabStoragePresenter.ViewDef {

    @Inject
    public MainTabStorageView(MainModelProvider<storage_domains, StorageListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new StorageDomainSharedStatusColumn(), "", "30px");

        TextColumn<storage_domains> nameColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_name();
            }
        };
        getTable().addColumn(nameColumn, "Domain Name");

        TextColumn<storage_domains> domainTypeColumn = new EnumColumn<storage_domains, StorageDomainType>() {
            @Override
            protected StorageDomainType getRawValue(storage_domains object) {
                return object.getstorage_domain_type();
            }
        };
        getTable().addColumn(domainTypeColumn, "Domain Type");

        TextColumn<storage_domains> storageTypeColumn = new EnumColumn<storage_domains, StorageType>() {
            @Override
            protected StorageType getRawValue(storage_domains object) {
                return object.getstorage_type();
            }
        };
        getTable().addColumn(storageTypeColumn, "Storage Type");

        TextColumn<storage_domains> formatColumn = new EnumColumn<storage_domains, StorageFormatType>() {
            @Override
            protected StorageFormatType getRawValue(storage_domains object) {
                return object.getStorageFormat();
            }
        };
        getTable().addColumn(formatColumn, "Format");

        TextColumn<storage_domains> crossDataCenterStatusColumn =
                new EnumColumn<storage_domains, StorageDomainSharedStatus>() {
                    @Override
                    protected StorageDomainSharedStatus getRawValue(storage_domains object) {
                        return object.getstorage_domain_shared_status();
                    }
                };
        getTable().addColumn(crossDataCenterStatusColumn, "Cross Data-Center Status");

        TextColumn<storage_domains> freeSpaceColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return String.valueOf(object.getTotalDiskSize() - object.getused_disk_size()) + " GB";
            }
        };
        getTable().addColumn(freeSpaceColumn, "Free Space");

        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>(getMainModel().getNewDomainCommand(),
                "New Domain"));
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>(getMainModel().getImportDomainCommand(),
                "Import Domain"));
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>(getMainModel().getEditCommand(),
                "Edit"));
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>(getMainModel().getRemoveCommand(),
                "Remove"));
    }

}
