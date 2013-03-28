package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.ReportActionsHelper;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.DcStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabDataCenterView extends AbstractMainTabWithDetailsTableView<storage_pool, DataCenterListModel> implements MainTabDataCenterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabDataCenterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabDataCenterView(MainModelProvider<storage_pool, DataCenterListModel> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(resources, constants);
        initWidget(getTable());
    }

    void initTable(ApplicationResources resources, ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new DcStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<storage_pool> nameColumn = new TextColumnWithTooltip<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, constants.nameDc(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<storage_pool> storageTypeColumn = new EnumColumn<storage_pool, StorageType>() {
            @Override
            public StorageType getRawValue(storage_pool object) {
                return object.getstorage_pool_type();
            }
        };
        getTable().addColumn(storageTypeColumn, constants.storgeTypeDc(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<storage_pool> statusColumn = new EnumColumn<storage_pool, StoragePoolStatus>() {
            @Override
            public StoragePoolStatus getRawValue(storage_pool object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusDc(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<storage_pool> versionColumn = new TextColumnWithTooltip<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, constants.comptVersDc(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<storage_pool> descColumn = new TextColumnWithTooltip<storage_pool>() {
            @Override
            public String getValue(storage_pool object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, constants.descriptionDc(), "300px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<storage_pool>(constants.newDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_pool>(constants.editDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<storage_pool>(constants.removeDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<storage_pool>(constants.forceRemoveDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getForceRemoveCommand();
            }
        });

        if (ReportInit.getInstance().isReportsEnabled()) {
            List<ActionButtonDefinition<storage_pool>> resourceSubActions =
                    ReportActionsHelper.getInstance().getResourceSubActions("DataCenter", getModelProvider()); //$NON-NLS-1$
            if (resourceSubActions != null && resourceSubActions.size() > 0) {
                getTable().addActionButton(new WebAdminMenuBarButtonDefinition<storage_pool>(constants.showReportDC(),
                        resourceSubActions));
            }
        }

        getTable().addActionButton(new WebAdminImageButtonDefinition<storage_pool>(constants.guideMeDc(),
                resources.guideSmallImage(), resources.guideSmallDisabledImage(), true) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getGuideCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<storage_pool>(constants.reinitializeDC(), CommandLocation.OnlyFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRecoveryStorageCommand();
            }
        });
    }
}
