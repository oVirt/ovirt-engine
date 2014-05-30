package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBricksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.BrickStatusColumn;

public class SubTabHostBrickView extends AbstractSubTabTableView<VDS, GlusterBrickEntity, HostListModel, HostBricksListModel>
        implements SubTabHostBrickPresenter.ViewDef {

    @Inject
    public SubTabHostBrickView(SearchableDetailModelProvider<GlusterBrickEntity, HostListModel, HostBricksListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new BrickStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<GlusterBrickEntity> volNameColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity object) {
                return object.getVolumeName();
            }
        };
        volNameColumn.makeSortable();
        getTable().addColumn(volNameColumn, constants.volumeName()); //$NON-NLS-1$

        TextColumnWithTooltip<GlusterBrickEntity> brickDirColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity object) {
                return object.getBrickDirectory();
            }
        };
        brickDirColumn.makeSortable();
        getTable().addColumn(brickDirColumn, constants.brickDirectoryBricks(), "220px"); //$NON-NLS-1$

   }

}
