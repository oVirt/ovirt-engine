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
import org.ovirt.engine.ui.webadmin.widget.table.column.BrickHealInfoColumn;
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

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabVolumeBrickView(SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        BrickStatusColumn brickStatusColumn = new BrickStatusColumn();
        brickStatusColumn.setContextMenuTitle(constants.statusVolumeBrick());
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
                String qualifiedName = brick.getQualifiedName();
                if(brick.getIsArbiter()){
                    qualifiedName += " (" + constants.arbiter() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                return qualifiedName;
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

        BrickHealInfoColumn healInfoColumn = new BrickHealInfoColumn();
        getTable().addColumn(healInfoColumn, constants.healInfo(), "110px"); //$NON-NLS-1$

        getTable().addColumn(new VolumeActivityColumn<GlusterBrickEntity>(getActivityCell()),
                constants.activitiesOnVolume(), "100px"); //$NON-NLS-1$
    }

    private VolumeActivityCompositeCell<GlusterTaskSupport> getActivityCell() {
        MenuCell<GlusterTaskSupport> removeBricksMenuCell = getRemoveBrickActivityMenu();
        List<HasCell<GlusterTaskSupport, ?>> list = new ArrayList<>();
        list.add(new VolumeActivityStatusColumn<>());
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
                new VolumeActivityCompositeCell<>(list);
        return activityCell;
    }

    private MenuCell<GlusterTaskSupport> getRemoveBrickActivityMenu() {
        MenuCell<GlusterTaskSupport> menuCell = new MenuCell<GlusterTaskSupport>() {
            @Override
            protected boolean isVisible(GlusterTaskSupport value) {
                return value.getAsyncTask().getType() == GlusterTaskType.REMOVE_BRICK;
            }
        };
        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.removeBricksStatus()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStatusRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.removeBricksStop()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStopRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.removeBricksCommit()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCommitRemoveBricksCommand();
            }
        });

        menuCell.addMenuItem(new WebAdminButtonDefinition<Void, GlusterTaskSupport>(constants.retainBricks()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRetainBricksCommand();
            }
        });

        return menuCell;
    }
}
