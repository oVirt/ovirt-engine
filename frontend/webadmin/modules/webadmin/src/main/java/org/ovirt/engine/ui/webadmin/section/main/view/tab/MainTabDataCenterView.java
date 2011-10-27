package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.DcStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class MainTabDataCenterView extends AbstractMainTabWithDetailsTableView<storage_pool, DataCenterListModel> implements MainTabDataCenterPresenter.ViewDef {

    @Inject
    public MainTabDataCenterView(MainModelProvider<storage_pool, DataCenterListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new DcStatusColumn(), "", "30px");

        TextColumn<storage_pool> nameColumn = new TextColumn<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<storage_pool> storageTypeColumn = new EnumColumn<storage_pool, StorageType>() {
            @Override
            public StorageType getRawValue(storage_pool object) {
                return object.getstorage_pool_type();
            }
        };
        getTable().addColumn(storageTypeColumn, "Storage Type");

        TextColumn<storage_pool> statusColumn = new EnumColumn<storage_pool, StoragePoolStatus>() {
            @Override
            public StoragePoolStatus getRawValue(storage_pool object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumn<storage_pool> versionColumn = new TextColumn<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, "Compatibility Version");

        TextColumn<storage_pool> descColumn = new TextColumn<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<storage_pool>(getMainModel().getNewCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<storage_pool>(getMainModel().getEditCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<storage_pool>(getMainModel().getRemoveCommand()));
    }

}
