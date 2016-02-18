package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVolumePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.cell.MenuCell;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeActivityCompositeCell;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeActivitySeperatorCell;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeCapacityCell;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeTaskWaitingCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeActivityColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeActivityStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeBrickStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeInfoColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeStatusColumn;

import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;

public class MainTabVolumeView extends AbstractMainTabWithDetailsTableView<GlusterVolumeEntity, VolumeListModel> implements MainTabVolumePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabVolumeView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabVolumeView(MainModelProvider<GlusterVolumeEntity, VolumeListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(modelProvider.getModel());
        initWidget(getTable());
    }

    void initTable(VolumeListModel model) {
        getTable().enableColumnResizing();

        VolumeStatusColumn statusColumn = new VolumeStatusColumn(model.getStartCommand());
        statusColumn.setContextMenuTitle(constants.statusVolume());
        statusColumn.makeSortable(new Comparator<GlusterVolumeEntity>() {
            @Override
            public int compare(GlusterVolumeEntity o1, GlusterVolumeEntity o2) {
                return GlusterVolumeUtils.getVolumeStatus(o1).ordinal() - GlusterVolumeUtils.getVolumeStatus(o2).ordinal();
            }
        });
        getTable().addColumn(statusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterVolumeEntity> nameColumn = new AbstractTextColumn<GlusterVolumeEntity>() {
            @Override
            public String getValue(GlusterVolumeEntity object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();

        getTable().addColumn(nameColumn, constants.nameVolume(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterVolumeEntity> clusterColumn = new AbstractTextColumn<GlusterVolumeEntity>() {
            @Override
            public String getValue(GlusterVolumeEntity object) {
                return object.getClusterName();
            }
        };
        clusterColumn.makeSortable();
        getTable().addColumn(clusterColumn, constants.clusterVolume(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterVolumeEntity> volumeTypeColumn =
                new AbstractEnumColumn<GlusterVolumeEntity, GlusterVolumeType>() {

            @Override
            protected GlusterVolumeType getRawValue(GlusterVolumeEntity object) {
                return object.getVolumeType();
            }
        };
        volumeTypeColumn.makeSortable();
        getTable().addColumn(volumeTypeColumn, constants.volumeTypeVolume(), "150px"); //$NON-NLS-1$

        getTable().addColumn(new VolumeBrickStatusColumn(), constants.bricksStatusVolume(), "150px"); //$NON-NLS-1$
        getTable().addColumn(new VolumeInfoColumn(), constants.volumeInfoVolume(), "100px"); //$NON-NLS-1$

        MenuCell<GlusterTaskSupport> rebalanceMenuCell = getRebalanceActivityMenu();
        MenuCell<GlusterTaskSupport> removeBricksMenuCell = getRemoveBrickActivityMenu();

        List<HasCell<GlusterTaskSupport, ?>> list = new ArrayList<>();
        list.add(new VolumeActivityStatusColumn<>());
        list.add(new Column<GlusterTaskSupport, GlusterTaskSupport>(new VolumeActivitySeperatorCell<GlusterTaskSupport>()) {
            @Override
            public GlusterTaskSupport getValue(GlusterTaskSupport object) {
                return object;
            }
        });
        list.add(new Column<GlusterTaskSupport, GlusterTaskSupport>(rebalanceMenuCell) {
            @Override
            public GlusterTaskSupport getValue(GlusterTaskSupport object) {
                return object;
            }
        });
        list.add(new Column<GlusterTaskSupport, GlusterTaskSupport>(removeBricksMenuCell) {
            @Override
            public GlusterTaskSupport getValue(GlusterTaskSupport object) {
                return object;
            }
        });

        List<HasCell<GlusterTaskSupport, ?>> compositeList = new ArrayList<>();
        compositeList.add(new Column<GlusterTaskSupport, GlusterTaskSupport>(new VolumeTaskWaitingCell<>()) {
            @Override
            public GlusterTaskSupport getValue(GlusterTaskSupport object) {
                return object;
            }
        });
        compositeList.add(new Column<GlusterTaskSupport, GlusterTaskSupport>(new VolumeActivityCompositeCell<>(list)) {
            @Override
            public GlusterTaskSupport getValue(GlusterTaskSupport object) {
                return object;
            }
        });

        Column<GlusterVolumeEntity, GlusterVolumeEntity> capacityColumn = new Column<GlusterVolumeEntity, GlusterVolumeEntity>(new VolumeCapacityCell()) {
            @Override
            public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
                return object;
            }
        };
        getTable().addColumn(capacityColumn, constants.volumeCapacity(), "100px");//$NON-NLS-1$

        getTable().addColumn(new VolumeActivityColumn<GlusterVolumeEntity>(
                new VolumeActivityCompositeCell<GlusterTaskSupport>(compositeList)),
        constants.activitiesOnVolume(),
        "100px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterVolumeEntity> snapshotCountColumn =
                new AbstractTextColumn<GlusterVolumeEntity>() {
            @Override
            public String getValue(GlusterVolumeEntity object) {
                return object.getSnapshotsCount().toString();
            }
        };
        snapshotCountColumn.makeSortable();
        getTable().addColumn(snapshotCountColumn, constants.noOfSnapshotsLabel(), "100px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.newVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewVolumeCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.removeVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveVolumeCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.startVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStartCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.stopVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStopCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.rebalanceVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStartRebalanceCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.optimizeForVirtStore()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getOptimizeForVirtStoreCommand();
            }
        });

        List<ActionButtonDefinition<GlusterVolumeEntity>> volumeProfilingActions = new LinkedList<>();
        volumeProfilingActions.add(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.startVolumeProfiling()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStartVolumeProfilingCommand();
            }
        });
        volumeProfilingActions.add(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.volumeProfileDetails()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getShowVolumeProfileDetailsCommand();
            }
        });
        volumeProfilingActions.add(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.stopVolumeProfiling()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStopVolumeProfilingCommand();
            }
        });

        getTable().addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.volumeProfilingAction(),
                volumeProfilingActions,
                CommandLocation.ContextAndToolBar));

        getTable().addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.volumeSnapshotMainTabTitle(),
                getVolumeSnapshotMenu(),
                CommandLocation.ContextAndToolBar));

        getTable().addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.geoReplicationMainTabTitle(),
                getGeoRepCreateMenu(constants),
                CommandLocation.ContextAndToolBar));
    }

    private List<ActionButtonDefinition<GlusterVolumeEntity>> getGeoRepCreateMenu(ApplicationConstants constants) {
        List<ActionButtonDefinition<GlusterVolumeEntity>> geoRepMenu = new ArrayList<>();
        WebAdminButtonDefinition<GlusterVolumeEntity> geoRepButton =
                new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.newGeoRepSession()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getMainModel().getNewGeoRepSessionCommand();
                    }
                };
        geoRepMenu.add(geoRepButton);
        return geoRepMenu;
    }

    private List<ActionButtonDefinition<GlusterVolumeEntity>> getVolumeSnapshotMenu() {
        List<ActionButtonDefinition<GlusterVolumeEntity>> snapshotMenu = new ArrayList<>();

        WebAdminButtonDefinition<GlusterVolumeEntity> newSnapshotButton =
                new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.newVolumeSnapshot()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getMainModel().getCreateSnapshotCommand();
                    }
                };
        snapshotMenu.add(newSnapshotButton);
        WebAdminButtonDefinition<GlusterVolumeEntity> editSnapshotScheduleButton =
                new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.editVolumeSnapshotSchedule()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getMainModel().getEditSnapshotScheduleCommand();
                    }
                };
        snapshotMenu.add(editSnapshotScheduleButton);

        WebAdminButtonDefinition<GlusterVolumeEntity> configureClusterSnapshotOptionsButton = new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.configureClusterSnapshotOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getConfigureClusterSnapshotOptionsCommand();
            }
        };
        WebAdminButtonDefinition<GlusterVolumeEntity> configureVolumeSnapshotOptionsButton = new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.configureVolumeSnapshotOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getConfigureVolumeSnapshotOptionsCommand();
            }
        };

        snapshotMenu.add(configureClusterSnapshotOptionsButton);
        snapshotMenu.add(configureVolumeSnapshotOptionsButton);

        return snapshotMenu;
    }

    private MenuCell<GlusterTaskSupport> getRebalanceActivityMenu() {
        MenuCell<GlusterTaskSupport> menuCell = new MenuCell<GlusterTaskSupport>() {
            @Override
            protected boolean isVisible(GlusterTaskSupport value) {
                return value.getAsyncTask() != null && value.getAsyncTask().getType() == GlusterTaskType.REBALANCE;
            }
        };

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.statusRebalance()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStatusRebalanceCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.stopRebalance()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStopRebalanceCommand();
            }
        });

        return menuCell;
    }

    private MenuCell<GlusterTaskSupport> getRemoveBrickActivityMenu() {
        MenuCell<GlusterTaskSupport> menuCell = new MenuCell<GlusterTaskSupport>() {
            @Override
            protected boolean isVisible(GlusterTaskSupport value) {
                return value.getAsyncTask() != null && value.getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK;
            }
        };

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.removeBricksStatus()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getBrickListModel()
                        .getStatusRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.removeBricksStop()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getBrickListModel()
                        .getStopRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.removeBricksCommit()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getBrickListModel()
                        .getCommitRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.retainBricks()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getBrickListModel()
                        .getRetainBricksCommand();
            }
        });

        return menuCell;
    }

}
