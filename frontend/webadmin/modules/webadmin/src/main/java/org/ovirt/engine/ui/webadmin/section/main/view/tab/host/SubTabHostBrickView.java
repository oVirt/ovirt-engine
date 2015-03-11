package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBricksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.BrickStatusColumn;

import com.google.gwt.core.client.GWT;

public class SubTabHostBrickView extends AbstractSubTabTableView<VDS, GlusterBrickEntity, HostListModel<Void>, HostBricksListModel>
        implements SubTabHostBrickPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostBrickView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabHostBrickView(SearchableDetailModelProvider<GlusterBrickEntity, HostListModel<Void>, HostBricksListModel> modelProvider, ApplicationConstants constants) {
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

        AbstractTextColumn<GlusterBrickEntity> volNameColumn = new AbstractTextColumn<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity object) {
                return object.getVolumeName();
            }
        };
        volNameColumn.makeSortable();
        getTable().addColumn(volNameColumn, constants.volumeName()); //$NON-NLS-1$

        AbstractTextColumn<GlusterBrickEntity> brickDirColumn = new AbstractTextColumn<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity object) {
                return object.getBrickDirectory();
            }
        };
        brickDirColumn.makeSortable();
        getTable().addColumn(brickDirColumn, constants.brickDirectoryBricks(), "220px"); //$NON-NLS-1$

   }

}
