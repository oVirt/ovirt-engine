package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.inject.Inject;

public class SubTabVolumeBrickView extends AbstractSubTabTableView<GlusterVolumeEntity, GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> implements SubTabVolumeBrickPresenter.ViewDef {

    @Inject
    public SubTabVolumeBrickView(SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<GlusterBrickEntity> serverColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return brick.getServerName();
            }
        };
        getTable().addColumn(serverColumn, "Server");

        TextColumnWithTooltip<GlusterBrickEntity> directoryColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return brick.getBrickDirectory();
            }
        };
        getTable().addColumn(directoryColumn, "Brick Directory");

        TextColumnWithTooltip<GlusterBrickEntity> freeSpaceColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return "???";
            }
        };
        getTable().addColumn(freeSpaceColumn, "Free Space (GB)");

        TextColumnWithTooltip<GlusterBrickEntity> totalSpaceColumn = new TextColumnWithTooltip<GlusterBrickEntity>() {
            @Override
            public String getValue(GlusterBrickEntity brick) {
                return "???";
            }
        };
        getTable().addColumn(totalSpaceColumn, "Total Space (GB)");

        TextColumnWithTooltip<GlusterBrickEntity> statusColumn =
                new EnumColumn<GlusterBrickEntity, GlusterBrickStatus>() {

                    @Override
                    protected GlusterBrickStatus getRawValue(GlusterBrickEntity object) {
                        return object.getStatus();
                    }
                };
        getTable().addColumn(statusColumn, "Status");
    }
}
