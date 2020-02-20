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

public class VolumeActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, GlusterVolumeEntity, VolumeListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, GlusterVolumeEntity> newButtonDefinition;

    @Inject
    public VolumeActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, GlusterVolumeEntity> view,
            MainModelProvider<GlusterVolumeEntity, VolumeListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.newVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewVolumeCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.removeVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveVolumeCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.startVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStartCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.stopVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStopCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.rebalanceVolume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStartRebalanceCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.optimizeForVirtStore()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getOptimizeForVirtStoreCommand();
            }
        });

        List<ActionButtonDefinition<Void, GlusterVolumeEntity>> volumeProfilingActions = new ArrayList<>();

        volumeProfilingActions.add(new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.startVolumeProfiling()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStartVolumeProfilingCommand();
            }
        });
        volumeProfilingActions.add(new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.volumeProfileDetails()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getShowVolumeProfileDetailsCommand();
            }
        });
        volumeProfilingActions.add(new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.stopVolumeProfiling()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStopVolumeProfilingCommand();
            }
        });
        addDropdownActionButton(new WebAdminMenuBarButtonDefinition<>(constants.volumeProfilingAction(),
                volumeProfilingActions));

        List<ActionButtonDefinition<Void, GlusterVolumeEntity>> volumeSnapshotActions = getVolumeSnapshotMenu();
        addDropdownActionButton(new WebAdminMenuBarButtonDefinition<>(constants.volumeSnapshotMainViewTitle(),
                volumeSnapshotActions));

        List<ActionButtonDefinition<Void, GlusterVolumeEntity>> volumeGeoRepActions = getGeoRepCreateMenu(constants);
        addDropdownActionButton(new WebAdminMenuBarButtonDefinition<>(constants.geoReplicationMainViewTitle(),
                volumeGeoRepActions));
    }

    private List<ActionButtonDefinition<Void, GlusterVolumeEntity>> getGeoRepCreateMenu(ApplicationConstants constants) {
        List<ActionButtonDefinition<Void, GlusterVolumeEntity>> geoRepMenu = new ArrayList<>();
        WebAdminButtonDefinition<Void, GlusterVolumeEntity> geoRepButton =
                new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.newGeoRepSession()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getModel().getNewGeoRepSessionCommand();
                    }
                };
        geoRepMenu.add(geoRepButton);
        return geoRepMenu;
    }

    private List<ActionButtonDefinition<Void, GlusterVolumeEntity>> getVolumeSnapshotMenu() {
        List<ActionButtonDefinition<Void, GlusterVolumeEntity>> snapshotMenu = new ArrayList<>();

        WebAdminButtonDefinition<Void, GlusterVolumeEntity> newSnapshotButton =
                new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.newVolumeSnapshot()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getModel().getCreateSnapshotCommand();
                    }
                };
        snapshotMenu.add(newSnapshotButton);
        WebAdminButtonDefinition<Void, GlusterVolumeEntity> editSnapshotScheduleButton =
                new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.editVolumeSnapshotSchedule()) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getModel().getEditSnapshotScheduleCommand();
                    }
                };
        snapshotMenu.add(editSnapshotScheduleButton);

        WebAdminButtonDefinition<Void, GlusterVolumeEntity> configureClusterSnapshotOptionsButton =
                new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.configureClusterSnapshotOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getConfigureClusterSnapshotOptionsCommand();
            }
        };
        WebAdminButtonDefinition<Void, GlusterVolumeEntity> configureVolumeSnapshotOptionsButton =
                new WebAdminButtonDefinition<Void, GlusterVolumeEntity>(constants.configureVolumeSnapshotOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getConfigureVolumeSnapshotOptionsCommand();
            }
        };

        snapshotMenu.add(configureClusterSnapshotOptionsButton);
        snapshotMenu.add(configureVolumeSnapshotOptionsButton);

        return snapshotMenu;
    }

    public WebAdminButtonDefinition<Void, GlusterVolumeEntity> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
