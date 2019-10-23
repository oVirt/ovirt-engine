package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class VolumeBrickActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<GlusterVolumeEntity, GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VolumeBrickActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<GlusterVolumeEntity, GlusterBrickEntity> view,
            SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterBrickEntity>(constants.addBricksBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddBricksCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterBrickEntity>(constants.removeBricksBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveBricksCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterBrickEntity>(constants.replaceBrickBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getReplaceBrickCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterBrickEntity>(constants.resetBrickBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getResetBrickCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity, GlusterBrickEntity>(constants.advancedDetailsBrick()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getBrickAdvancedDetailsCommand();
            }
        });
    }

}
