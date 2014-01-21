package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.cell.MenuCell;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeActivityCompositeCell;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeActivitySeperatorCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.BrickCapacityCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.BrickStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeActivityColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeActivityStatusColumn;

import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;

public class SubTabVolumeBrickView extends AbstractSubTabTableView<GlusterVolumeEntity, GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> implements SubTabVolumeBrickPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVolumeBrickView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabVolumeBrickView(SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        BrickStatusColumn brickStatusColumn = new BrickStatusColumn();
        brickStatusColumn.makeSortable();
        getTable().addColumn(brickStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterBrickEntity> serverColumn = new AbstractTextColumn<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return brick.getServerName();
            }
        };
        serverColumn.makeSortable();
        getTable().addColumn(serverColumn, constants.serverVolumeBrick(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterBrickEntity> directoryColumn = new AbstractTextColumn<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return brick.getQualifiedName();
            }
        };
        directoryColumn.makeSortable();

        getTable().addColumn(directoryColumn, constants.brickDirectoryVolumeBrick(), "400px"); //$NON-NLS-1$

        getTable().addColumn(new Column<GlusterBrickEntity, BrickProperties>( new BrickCapacityCell()) {
            @Override
            public BrickProperties getValue(GlusterBrickEntity object) {
                return object.getBrickProperties();
            }
        }, constants.volumeCapacity(), "100px");//$NON-NLS-1$

        getTable().addColumn(new VolumeActivityColumn<GlusterBrickEntity>(getActivityCell()),
                constants.activitiesOnVolume(), "100px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterBrickEntity>(constants.addBricksBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddBricksCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterBrickEntity>(constants.removeBricksBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveBricksCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterBrickEntity>(constants.replaceBrickBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getReplaceBrickCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterBrickEntity>(constants.advancedDetailsBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getBrickAdvancedDetailsCommand();
            }
        });
    }

    private VolumeActivityCompositeCell<GlusterTaskSupport> getActivityCell() {
        MenuCell<GlusterTaskSupport> removeBricksMenuCell = getRemoveBrickActivityMenu();
        List<HasCell<GlusterTaskSupport, ?>> list = new ArrayList<HasCell<GlusterTaskSupport, ?>>();
        list.add(new VolumeActivityStatusColumn<GlusterTaskSupport>());
        list.add(new Column<GlusterTaskSupport, GlusterTaskSupport>(new VolumeActivitySeperatorCell<GlusterTaskSupport>()) {
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

        VolumeActivityCompositeCell<GlusterTaskSupport> activityCell =
                new VolumeActivityCompositeCell<GlusterTaskSupport>(list) {
                    @Override
                    protected boolean isVisible(GlusterTaskSupport value) {
                        return super.isVisible(value) && value.getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK;
                    }
                };
        return activityCell;
    }

    private MenuCell<GlusterTaskSupport> getRemoveBrickActivityMenu() {
        MenuCell<GlusterTaskSupport> menuCell = new MenuCell<GlusterTaskSupport>() {
            @Override
            protected boolean isVisible(GlusterTaskSupport value) {
                return value.getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK;
            }
        };
        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.removeBricksStatus()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStatusRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.removeBricksStop()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStopRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.removeBricksCommit()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCommitRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<GlusterTaskSupport>(constants.retainBricks()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRetainBricksCommand();
            }
        });

        return menuCell;
    }
}
