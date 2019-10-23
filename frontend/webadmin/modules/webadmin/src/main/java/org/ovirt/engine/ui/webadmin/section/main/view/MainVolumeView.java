package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainVolumePresenter;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
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

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;

public class MainVolumeView extends AbstractMainWithDetailsTableView<GlusterVolumeEntity, VolumeListModel> implements MainVolumePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainVolumeView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainVolumeView(MainModelProvider<GlusterVolumeEntity, VolumeListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(modelProvider.getModel());
        initWidget(getTable());
    }

    void initTable(VolumeListModel model) {
        getTable().enableColumnResizing();

        VolumeStatusColumn statusColumn = new VolumeStatusColumn(model.getStartCommand());
        statusColumn.setContextMenuTitle(constants.statusVolume());
        statusColumn.makeSortable(Comparator.comparingInt(g -> GlusterVolumeUtils.getVolumeStatus(g).ordinal()));
        getTable().addColumn(statusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterVolumeEntity> nameColumn =
                new AbstractLinkColumn<GlusterVolumeEntity>(new FieldUpdater<GlusterVolumeEntity, String>() {

            @Override
            public void update(int index, GlusterVolumeEntity volume, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), volume.getName());
                //The link was clicked, now fire an event to switch to details.
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.volumeGeneralSubTabPlace, parameters);
            }

        }) {
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

    }

    private MenuCell<GlusterTaskSupport> getRebalanceActivityMenu() {
        MenuCell<GlusterTaskSupport> menuCell = new MenuCell<GlusterTaskSupport>() {
            @Override
            protected boolean isVisible(GlusterTaskSupport value) {
                return value.getAsyncTask() != null && value.getAsyncTask().getType() == GlusterTaskType.REBALANCE;
            }
        };

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.statusRebalance()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getStatusRebalanceCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.stopRebalance()) {
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

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.removeBricksStatus()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getBrickListModel()
                        .getStatusRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.removeBricksStop()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getBrickListModel()
                        .getStopRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.removeBricksCommit()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getBrickListModel()
                        .getCommitRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.retainBricks()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getBrickListModel()
                        .getRetainBricksCommand();
            }
        });

        return menuCell;
    }

}
