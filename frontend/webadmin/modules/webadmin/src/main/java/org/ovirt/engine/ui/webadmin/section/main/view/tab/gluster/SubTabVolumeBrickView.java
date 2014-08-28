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
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.BrickStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.MenuCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.PercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeActivityColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeActivityCompositeCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeActivitySeperatorCell;
import org.ovirt.engine.ui.webadmin.widget.table.column.VolumeActivityStatusColumn;

import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;

public class SubTabVolumeBrickView extends AbstractSubTabTableView<GlusterVolumeEntity, GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> implements SubTabVolumeBrickPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVolumeBrickView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVolumeBrickView(SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        BrickStatusColumn brickStatusColumn = new BrickStatusColumn();
        brickStatusColumn.makeSortable();
        getTable().addColumn(brickStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<GlusterBrickEntity> serverColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return brick.getServerName();
            }
        };
        serverColumn.makeSortable();
        getTable().addColumn(serverColumn, constants.serverVolumeBrick(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<GlusterBrickEntity> directoryColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return brick.getBrickDirectory();
            }
        };
        directoryColumn.makeSortable();

        getTable().addColumn(directoryColumn, constants.brickDirectoryVolumeBrick(), "400px"); //$NON-NLS-1$

        getTable().addColumn(new PercentColumn<GlusterBrickEntity>() {
            @Override
            protected Integer getProgressValue(GlusterBrickEntity object) {
                if(object.getBrickProperties() == null) {
                    return 0;
                }
                BrickProperties brickProperties = object.getBrickProperties();
                return (int)(((brickProperties.getTotalSize() - brickProperties.getFreeSize())/ (brickProperties.getTotalSize())) * 100);
            }
        }, constants.volumeCapacity(), "60px");//$NON-NLS-1$

        getTable().addColumn(new VolumeActivityColumn<GlusterBrickEntity>(getActivityCell(constants)),
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

    private VolumeActivityCompositeCell<GlusterTaskSupport> getActivityCell(ApplicationConstants constants) {
        MenuCell<GlusterTaskSupport> removeBricksMenuCell = getRemoveBrickActivityMenu(constants);
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

    private MenuCell<GlusterTaskSupport> getRemoveBrickActivityMenu(ApplicationConstants constants) {
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
