package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStage;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGlusterHookListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGlusterHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.GlusterHookSyncStatusColumn;

import com.google.gwt.core.client.GWT;

public class SubTabClusterGlusterHookView
        extends
        AbstractSubTabTableView<Cluster, GlusterHookEntity, ClusterListModel<Void>, ClusterGlusterHookListModel>
        implements SubTabClusterGlusterHookPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabClusterGlusterHookView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabClusterGlusterHookView(
            SearchableDetailModelProvider<GlusterHookEntity, ClusterListModel<Void>, ClusterGlusterHookListModel> modelProvider) {
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

        getTable().addColumn(new GlusterHookSyncStatusColumn(),
                constants.empty(), "10px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterHookEntity> nameColumn = new AbstractTextColumn<GlusterHookEntity>() {
            @Override
            public String getValue(GlusterHookEntity object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameHook(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterHookEntity> statusColumn = new AbstractEnumColumn<GlusterHookEntity, GlusterHookStatus>() {

            @Override
            protected GlusterHookStatus getRawValue(GlusterHookEntity object) {
                return object.getStatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusHook(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterHookEntity> glusterCommandColumn = new AbstractTextColumn<GlusterHookEntity>() {
            @Override
            public String getValue(GlusterHookEntity object) {
                return object.getGlusterCommand();
            }
        };

        getTable().addColumn(glusterCommandColumn,
                constants.glusterVolumeEventHook(), "100px"); //$NON-NLS-1$;

        AbstractTextColumn<GlusterHookEntity> stageColumn = new AbstractEnumColumn<GlusterHookEntity, GlusterHookStage>() {

            @Override
            protected GlusterHookStage getRawValue(GlusterHookEntity object) {
                return object.getStage();
            }
        };
        getTable().addColumn(stageColumn, constants.stageHook(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterHookEntity> contentTypeColumn =
                new AbstractEnumColumn<GlusterHookEntity, GlusterHookContentType>() {

                    @Override
                    protected GlusterHookContentType getRawValue(
                            GlusterHookEntity object) {
                        return object.getContentType();
                    }
                };
        getTable().addColumn(contentTypeColumn, constants.contentTypeHook(), "150px"); //$NON-NLS-1$
    }
}
