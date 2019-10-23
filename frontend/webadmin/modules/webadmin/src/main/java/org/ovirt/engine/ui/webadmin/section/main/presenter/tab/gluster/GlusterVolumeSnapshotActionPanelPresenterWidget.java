package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class GlusterVolumeSnapshotActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<GlusterVolumeEntity, GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public GlusterVolumeSnapshotActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<GlusterVolumeEntity, GlusterVolumeSnapshotEntity> view,
            SearchableDetailModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel,
                GlusterVolumeSnapshotListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterVolumeSnapshotEntity>(constants.restoreVolumeSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRestoreSnapshotCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterVolumeSnapshotEntity>(constants.deleteVolumeSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDeleteSnapshotCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterVolumeSnapshotEntity>(constants.deleteAllVolumeSnapshots()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDeleteAllSnapshotsCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterVolumeSnapshotEntity>(constants.activateVolumeSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getActivateSnapshotCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterVolumeSnapshotEntity>(constants.deactivateVolumeSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDeactivateSnapshotCommand();
            }
        });
    }

}
