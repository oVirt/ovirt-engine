package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.BrickStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabVolumeBrickView extends AbstractSubTabTableView<GlusterVolumeEntity, GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> implements SubTabVolumeBrickPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVolumeBrickView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVolumeBrickView(SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new BrickStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<GlusterBrickEntity> serverColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return brick.getServerName();
            }
        };
        getTable().addColumn(serverColumn, constants.serverVolumeBrick(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<GlusterBrickEntity> directoryColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return brick.getBrickDirectory();
            }
        };
        getTable().addColumn(directoryColumn, constants.brickDirectoryVolumeBrick(), "400px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<GlusterBrickEntity>(constants.addBricksBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddBricksCommand();
            }
        });

        List<ActionButtonDefinition<GlusterBrickEntity>> removeSubActions = new LinkedList<ActionButtonDefinition<GlusterBrickEntity>>();
        removeSubActions.add(new WebAdminButtonDefinition<GlusterBrickEntity>(constants.removeBricksStart()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveBricksCommand();
            }
        });

        removeSubActions.add(new WebAdminButtonDefinition<GlusterBrickEntity>(constants.removeBricksStop()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStopRemoveBricksCommand();
            }
        });

        removeSubActions.add(new WebAdminButtonDefinition<GlusterBrickEntity>(constants.removeBricksCommit()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCommitRemoveBricksCommand();
            }
        });

        getTable().addActionButton(new WebAdminMenuBarButtonDefinition<GlusterBrickEntity>(constants.removeBricksBrick(),
                removeSubActions,
                CommandLocation.ContextAndToolBar));

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
}
