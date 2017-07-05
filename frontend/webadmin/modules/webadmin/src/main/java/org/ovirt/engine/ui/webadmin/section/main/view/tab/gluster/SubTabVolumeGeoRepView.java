package org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeGeoRepPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabVolumeGeoRepView
extends
AbstractSubTabTableView<GlusterVolumeEntity, GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel>
implements SubTabVolumeGeoRepPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVolumeGeoRepView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabVolumeGeoRepView(SearchableDetailModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> modelProvider) {
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
        getTable().addColumn(new AbstractTextColumn<GlusterGeoRepSession>() {
            @Override
            public String getValue(GlusterGeoRepSession object) {
                return object.getSlaveHostName();
            }
        }, constants.volumeSubTabGeoRepSlaveClusterHostColumn(), "220px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractTextColumn<GlusterGeoRepSession>() {
            @Override
            public String getValue(GlusterGeoRepSession object) {
                return object.getSlaveVolumeName();
            }
        }, constants.volumeSubTabGeoRepSlaveVolumeColumn(), "220px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractTextColumn<GlusterGeoRepSession>() {
            @Override
            public String getValue(GlusterGeoRepSession object) {
                return object.getUserName() != null ? object.getUserName() : "root"; //$NON-NLS-1$
            }
        }, constants.volumeSubTabGeoRepSlaveUserColumn(), "150px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractTextColumn<GlusterGeoRepSession>() {
            @Override
            public String getValue(GlusterGeoRepSession object) {
                return object.getStatus().toString();
            }
        }, constants.volumeSubTabGeoRepStatusColumn(), "150px"); //$NON-NLS-1$
    }
}
