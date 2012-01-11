package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.DcStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabDataCenterView extends AbstractMainTabWithDetailsTableView<storage_pool, DataCenterListModel> implements MainTabDataCenterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabDataCenterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabDataCenterView(MainModelProvider<storage_pool, DataCenterListModel> modelProvider,
            ApplicationResources resources) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources);
        initWidget(getTable());
    }

    void initTable(ApplicationResources resources) {
        getTable().addColumn(new DcStatusColumn(), "", "30px");

        TextColumnWithTooltip<storage_pool> nameColumn = new TextColumnWithTooltip<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<storage_pool> storageTypeColumn = new EnumColumn<storage_pool, StorageType>() {
            @Override
            public StorageType getRawValue(storage_pool object) {
                return object.getstorage_pool_type();
            }
        };
        getTable().addColumn(storageTypeColumn, "Storage Type");

        TextColumnWithTooltip<storage_pool> statusColumn = new EnumColumn<storage_pool, StoragePoolStatus>() {
            @Override
            public StoragePoolStatus getRawValue(storage_pool object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumnWithTooltip<storage_pool> versionColumn = new TextColumnWithTooltip<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, "Compatibility Version");

        TextColumnWithTooltip<storage_pool> descColumn = new TextColumnWithTooltip<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<storage_pool>("New") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<storage_pool>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<storage_pool>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<storage_pool>("Force Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getForceRemoveCommand();
            }
        });

        getTable().addActionButton(new ImageUiCommandButtonDefinition<storage_pool>("Guide Me",
                resources.guideSmallImage(), resources.guideSmallDisabledImage(), true) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getGuideCommand();
            }
        });
    }

}
