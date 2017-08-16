package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class VolumeActionPanelPresenterWidget extends ActionPanelPresenterWidget<GlusterVolumeEntity, VolumeListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<GlusterVolumeEntity> newButtonDefinition;

    @Inject
    public VolumeActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<GlusterVolumeEntity> view,
            MainModelProvider<GlusterVolumeEntity, VolumeListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.newVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewVolumeCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.removeVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveVolumeCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.startVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStartCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.stopVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStopCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.rebalanceVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStartRebalanceCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.optimizeForVirtStore()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getOptimizeForVirtStoreCommand();
            }
        });

        List<ActionButtonDefinition<GlusterVolumeEntity>> volumeProfilingActions = new ArrayList<>();

        volumeProfilingActions.add(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.startVolumeProfiling()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStartVolumeProfilingCommand();
            }
        });
        volumeProfilingActions.add(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.volumeProfileDetails()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getShowVolumeProfileDetailsCommand();
            }
        });
        volumeProfilingActions.add(new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.stopVolumeProfiling()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStopVolumeProfilingCommand();
            }
        });
        addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.volumeProfilingAction(),
                volumeProfilingActions), volumeProfilingActions);

        List<ActionButtonDefinition<GlusterVolumeEntity>> volumeSnapshotActions = getVolumeSnapshotMenu();
        addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.volumeSnapshotMainViewTitle(),
                volumeSnapshotActions), volumeSnapshotActions);

        List<ActionButtonDefinition<GlusterVolumeEntity>> volumeGeoRepActions = getGeoRepCreateMenu(constants);
        addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.geoReplicationMainViewTitle(),
                volumeGeoRepActions), volumeGeoRepActions);
    }

    private List<ActionButtonDefinition<GlusterVolumeEntity>> getGeoRepCreateMenu(ApplicationConstants constants) {
        List<ActionButtonDefinition<GlusterVolumeEntity>> geoRepMenu = new ArrayList<>();
        WebAdminButtonDefinition<GlusterVolumeEntity> geoRepButton =
                new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.newGeoRepSession()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getModel().getNewGeoRepSessionCommand();
                    }
                };
        geoRepMenu.add(geoRepButton);
        return geoRepMenu;
    }

    private List<ActionButtonDefinition<GlusterVolumeEntity>> getVolumeSnapshotMenu() {
        List<ActionButtonDefinition<GlusterVolumeEntity>> snapshotMenu = new ArrayList<>();

        WebAdminButtonDefinition<GlusterVolumeEntity> newSnapshotButton =
                new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.newVolumeSnapshot()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getModel().getCreateSnapshotCommand();
                    }
                };
        snapshotMenu.add(newSnapshotButton);
        WebAdminButtonDefinition<GlusterVolumeEntity> editSnapshotScheduleButton =
                new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.editVolumeSnapshotSchedule()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getModel().getEditSnapshotScheduleCommand();
                    }
                };
        snapshotMenu.add(editSnapshotScheduleButton);

        WebAdminButtonDefinition<GlusterVolumeEntity> configureClusterSnapshotOptionsButton =
                new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.configureClusterSnapshotOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getConfigureClusterSnapshotOptionsCommand();
            }
        };
        WebAdminButtonDefinition<GlusterVolumeEntity> configureVolumeSnapshotOptionsButton =
                new WebAdminButtonDefinition<GlusterVolumeEntity>(constants.configureVolumeSnapshotOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getConfigureVolumeSnapshotOptionsCommand();
            }
        };

        snapshotMenu.add(configureClusterSnapshotOptionsButton);
        snapshotMenu.add(configureVolumeSnapshotOptionsButton);

        return snapshotMenu;
    }

    public WebAdminButtonDefinition<GlusterVolumeEntity> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
