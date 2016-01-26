package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.searchbackend.StoragePoolFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.ReportActionsHelper;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.DcStatusColumn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class MainTabDataCenterView extends AbstractMainTabWithDetailsTableView<StoragePool, DataCenterListModel> implements MainTabDataCenterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabDataCenterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabDataCenterView(MainModelProvider<StoragePool, DataCenterListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        DcStatusColumn statusIconColumn = new DcStatusColumn();
        statusIconColumn.setContextMenuTitle(constants.statusIconDc());
        getTable().addColumn(statusIconColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> nameColumn = new AbstractTextColumn<StoragePool>() {
            @Override
            public String getValue(StoragePool object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(StoragePoolFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.nameDc(), "150px"); //$NON-NLS-1$

        CommentColumn<StoragePool> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> storageTypeColumn = new AbstractBooleanColumn<StoragePool>(
                constants.storageTypeLocal(), constants.storageTypeShared()) {
            @Override
            protected Boolean getRawValue(StoragePool object) {
                return object.isLocal();
            }
        };
        storageTypeColumn.makeSortable(StoragePoolFieldAutoCompleter.LOCAL);
        getTable().addColumn(storageTypeColumn, constants.storgeTypeDc(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> statusColumn = new AbstractEnumColumn<StoragePool, StoragePoolStatus>() {
            @Override
            public StoragePoolStatus getRawValue(StoragePool object) {
                return object.getStatus();
            }
        };
        statusColumn.makeSortable(StoragePoolFieldAutoCompleter.STATUS);
        getTable().addColumn(statusColumn, constants.statusDc(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> versionColumn = new AbstractTextColumn<StoragePool>() {
            @Override
            public String getValue(StoragePool object) {
                return object.getCompatibilityVersion().getValue();
            }
        };
        versionColumn.makeSortable(StoragePoolFieldAutoCompleter.COMPATIBILITY_VERSION);
        getTable().addColumn(versionColumn, constants.comptVersDc(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StoragePool> descColumn = new AbstractTextColumn<StoragePool>() {
            @Override
            public String getValue(StoragePool object) {
                return object.getdescription();
            }
        };
        descColumn.makeSortable(StoragePoolFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descColumn, constants.descriptionDc(), "300px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<StoragePool>(constants.newDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<StoragePool>(constants.editDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<StoragePool>(constants.removeDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<StoragePool>(constants.forceRemoveDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getForceRemoveCommand();
            }
        });

        if (ReportInit.getInstance().isReportsEnabled()) {
            updateReportsAvailability();
        } else {
            getMainModel().getReportsAvailabilityEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    updateReportsAvailability();
                }
            });
        }

        getTable().addActionButton(new WebAdminImageButtonDefinition<StoragePool>(constants.guideMeDc(),
                resources.guideSmallImage(), resources.guideSmallDisabledImage(), true) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getGuideCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<StoragePool>(constants.reinitializeDC(), CommandLocation.OnlyFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRecoveryStorageCommand();
            }
        });
    }

    private void updateReportsAvailability() {
        if (ReportInit.getInstance().isReportsEnabled()) {
            List<ActionButtonDefinition<StoragePool>> resourceSubActions =
                    ReportActionsHelper.getInstance().getResourceSubActions("DataCenter", getModelProvider()); //$NON-NLS-1$
            if (resourceSubActions != null && resourceSubActions.size() > 0) {
                getTable().addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.showReportDC(),
                        resourceSubActions));
            }
        }
    }
}
