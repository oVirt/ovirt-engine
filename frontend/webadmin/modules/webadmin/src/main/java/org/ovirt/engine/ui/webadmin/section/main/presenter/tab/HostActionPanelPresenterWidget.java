package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class HostActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, VDS, HostListModel<Void>> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, VDS> newButtonDefinition;

    @Inject
    public HostActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, VDS> view,
            MainModelProvider<VDS, HostListModel<Void>> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, VDS>(constants.newHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<Void, VDS>(constants.editHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, VDS>(constants.removeHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        // Management operations drop down
        List<ActionButtonDefinition<Void, VDS>> managementSubActions = new LinkedList<>();
        // Maintenance button
        managementSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.maintenanceHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getMaintenanceCommand();
            }
        });
        // Activate button
        managementSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.activateHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getActivateCommand();
            }
        });
        // Refresh capabilities button
        managementSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.refreshHostCapabilities()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRefreshCapabilitiesCommand();
            }
        });
        // Confirm Host Rebooted button
        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            addMenuListItem(new WebAdminButtonDefinition<Void, VDS>(constants.confirmRebootedHost()) {
                @Override
                protected UICommand resolveCommand() {
                    return getModel().getManualFenceCommand();
                }
            });
        }
        // Power management drop down
        List<ActionButtonDefinition<Void, VDS>> pmSubActions = new LinkedList<>();

        pmSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.restartHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRestartCommand();
            }
        });

        pmSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.startHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStartCommand();
            }
        });

        pmSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.stopHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStopCommand();
            }
        });
        // Remote management via SSH drop down
        List<ActionButtonDefinition<Void, VDS>> sshSubActions = new LinkedList<>();

        sshSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.restartHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getSshRestartCommand();
            }
        });
        sshSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.stopHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getSshStopCommand();
            }
        });

        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            managementSubActions.add(
                new WebAdminMenuBarButtonDefinition<Void, VDS>(
                    constants.pmHost(),
                    pmSubActions
                )
            );
            managementSubActions.add(
                new WebAdminMenuBarButtonDefinition<Void, VDS>(
                    constants.sshManagement(),
                    sshSubActions
                )
            );
        }
        // Select as SPM button
        managementSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.selectHostAsSPM()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getSelectAsSpmCommand();
            }
        });
        // Configure local storage button
        managementSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.configureLocalStorageHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getConfigureLocalStorageCommand();
            }
        });

        // Add management menu bar
        addDropdownActionButton(new WebAdminMenuBarButtonDefinition<Void, VDS>(constants.management(),
                managementSubActions));

        // Installation operations drop down
        List<ActionButtonDefinition<Void, VDS>> moreSubActions = new LinkedList<>();
        // Reinstall button
        moreSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.reinstallHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getInstallCommand();
            }
        });
        // Enroll certificate button
        moreSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.enrollCertificate()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEnrollCertificateCommand();
            }
        });
        // Check for upgrade button
        moreSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.checkForHostUpgrade()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCheckForUpgradeCommand();
            }
        });
        // Upgrade button
        moreSubActions.add(new WebAdminButtonDefinition<Void, VDS>(constants.upgradeHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUpgradeCommand();
            }
        });
        addDropdownActionButton(new WebAdminMenuBarButtonDefinition<>(constants.installation(), moreSubActions));

        // Host Console (link to Cockpit)
        addActionButton(new WebAdminButtonDefinition<Void, VDS>(constants.hostConsole()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getHostConsoleCommand();
            }
        });

        // Assign tags
        addMenuListItem(new WebAdminButtonDefinition<Void, VDS>(constants.assignTagsHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAssignTagsCommand();
            }
        });

        // NUMA support
        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly) {
            addMenuListItem(new WebAdminButtonDefinition<Void, VDS>(constants.numaSupport()) {
                @Override
                protected UICommand resolveCommand() {
                    return getModel().getNumaSupportCommand();
                }
            });
        }

        // Approve
        addMenuListItem(new WebAdminButtonDefinition<Void, VDS>(constants.approveHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getApproveCommand();
            }
        });

        // HA global maintenance
        addMenuListItem(new WebAdminButtonDefinition<Void, VDS>(constants.enableGlobalHaMaintenanceVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEnableGlobalHaMaintenanceCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, VDS>(constants.disableGlobalHaMaintenanceVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getDisableGlobalHaMaintenanceCommand();
            }
        });

    }

    public WebAdminButtonDefinition<Void, VDS> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
