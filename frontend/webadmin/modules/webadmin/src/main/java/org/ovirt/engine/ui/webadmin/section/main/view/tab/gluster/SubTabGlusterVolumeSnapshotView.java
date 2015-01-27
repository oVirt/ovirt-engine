package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabGlusterVolumeSnapshotPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.GlusterVolumeSnapshotStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Inject;

public class SubTabGlusterVolumeSnapshotView extends AbstractSubTabTableView<GlusterVolumeEntity, GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> implements SubTabGlusterVolumeSnapshotPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabGlusterVolumeSnapshotView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabGlusterVolumeSnapshotView(SearchableDetailModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> modelProvider,
            ApplicationConstants constants) {
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

        GlusterVolumeSnapshotStatusColumn snapshotStatusColumn = new GlusterVolumeSnapshotStatusColumn();
        snapshotStatusColumn.makeSortable();
        getTable().addColumn(snapshotStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<GlusterVolumeSnapshotEntity> snapshotNameColumn =
                new AbstractTextColumnWithTooltip<GlusterVolumeSnapshotEntity>() {
                    @Override
                    public String getValue(GlusterVolumeSnapshotEntity snapshot) {
                        return snapshot.getSnapshotName();
                    }
                };
        snapshotNameColumn.makeSortable();
        getTable().addColumn(snapshotNameColumn, constants.volumeSnapshotName(), "300px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<GlusterVolumeSnapshotEntity> descriptionColumn =
                new AbstractTextColumnWithTooltip<GlusterVolumeSnapshotEntity>() {
                    @Override
                    public String getValue(GlusterVolumeSnapshotEntity snapshot) {
                        return snapshot.getDescription();
                    }
                };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.volumeSnapshotDescription(), "400px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<GlusterVolumeSnapshotEntity> creationTimeColumn =
                new AbstractTextColumnWithTooltip<GlusterVolumeSnapshotEntity>() {
                    @Override
                    public String getValue(GlusterVolumeSnapshotEntity snapshot) {
                        DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd, HH:mm:ss"); //$NON-NLS-1$
                        return df.format(snapshot.getCreatedAt());
                    }
                };
        creationTimeColumn.makeSortable();
        getTable().addColumn(creationTimeColumn, constants.volumeSnapshotCreationTime(), "400px"); //$NON-NLS-1$
    }
}
