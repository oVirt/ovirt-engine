package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabStorageDataCenterView extends AbstractSubTabTableView<storage_domains, storage_domains, StorageListModel, StorageDataCenterListModel>
        implements SubTabStorageDataCenterPresenter.ViewDef {

    @Inject
    public SubTabStorageDataCenterView(SearchableDetailModelProvider<storage_domains, StorageListModel, StorageDataCenterListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new StorageDomainStatusColumn(), "", "30px");

        TextColumn<storage_domains> nameColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_pool_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<storage_domains> domainStatusColumn = new EnumColumn<storage_domains, StorageDomainStatus>() {
            @Override
            protected StorageDomainStatus getRawValue(storage_domains object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(domainStatusColumn, "Domain Status in Data-Center", "300px");

        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>(getDetailModel().getAttachCommand(),
                "Attach"));
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>(getDetailModel().getDetachCommand(),
                "Detach"));
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>(getDetailModel().getActivateCommand(),
                "Activate"));
        getTable().addActionButton(new UiCommandButtonDefinition<storage_domains>(getDetailModel().getMaintenanceCommand(),
                "Maintenance"));
    }

}
