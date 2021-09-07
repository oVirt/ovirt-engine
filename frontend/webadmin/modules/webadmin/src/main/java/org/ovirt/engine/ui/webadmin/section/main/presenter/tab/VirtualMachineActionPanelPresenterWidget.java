package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class VirtualMachineActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, VM, VmListModel<Void>> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, VM> newButtonDefinition;

    @Inject
    public VirtualMachineActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, VM> view,
            MainModelProvider<VM, VmListModel<Void>> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, VM>(constants.newVm()) {

            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewVmCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.restoreVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getImportVmCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, VM>(constants.editVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.cloneVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneVmCommand();
            }
        });

        List<ActionButtonDefinition<Void, VM>> runSubActions = new LinkedList<>();
        runSubActions.add(new UiCommandButtonDefinition<Void, VM>(getSharedEventBus(), constants.runOnceVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRunOnceCommand();
            }
        });
        addComboActionButton(new WebAdminImageButtonDefinition<Void, VM>(constants.runVm(), IconType.PLAY, runSubActions) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRunCommand();
            }
        });
        addActionButton(new WebAdminImageButtonDefinition<Void, VM>(constants.suspendVm(), IconType.MOON_O) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getSuspendCommand();
            }
        });

        List<ActionButtonDefinition<Void, VM>> shutdownSubActions = new LinkedList<>();
        shutdownSubActions.add(new WebAdminImageButtonDefinition<Void, VM>(constants.powerOffVm(), IconType.POWER_OFF) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStopCommand();
            }
        });
        addComboActionButtonWithContexts(
                new WebAdminImageButtonDefinition<Void, VM>(constants.shutDownVm(), IconType.STOP, shutdownSubActions) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getModel().getShutdownCommand();
                    }
                });

        List<ActionButtonDefinition<Void, VM>> rebootSubActions = new LinkedList<>();
        rebootSubActions.add(new WebAdminImageButtonDefinition<Void, VM>(constants.resetVm(), IconType.REPEAT) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getResetCommand();
            }
        });
        addComboActionButtonWithContexts(
                new WebAdminImageButtonDefinition<Void, VM>(constants.rebootVm(), IconType.REPEAT, rebootSubActions) {
                    @Override
                    protected UICommand resolveCommand() {
                        return getModel().getRebootCommand();
                    }
                });

        List<ActionButtonDefinition<Void, VM>> consoleOptionsSubActions = new LinkedList<>();
        consoleOptionsSubActions.add(new UiCommandButtonDefinition<Void, VM>(getSharedEventBus(),
                constants.consoleOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditConsoleCommand();
            }
        });

        addComboActionButton(new WebAdminImageButtonDefinition<Void, VM>(constants.consoleVm(), IconType.DESKTOP, consoleOptionsSubActions) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getConsoleConnectCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, VM>(constants.createSnapshotVM()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCreateSnapshotCommand();
            }
        });

        addDividerToKebab();

        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.removeVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        addDividerToKebab();

        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.changeCdVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getChangeCdCommand();
            }
        });
        addDividerToKebab();

        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.cancelMigrationVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCancelMigrateCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.cancelConvertVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCancelConvertCommand();
            }
        });
        addDividerToKebab();
        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.makeTemplateVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewTemplateCommand();
            }
        });
        addDividerToKebab();
        // Management operations drop down
        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.exportToExportDomain()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getExportCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.exportToOva()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getExportOvaCommand();
            }
        });
        addDividerToKebab();
        addMenuListItem(new WebAdminButtonDefinition<Void, VM>(constants.assignTagsVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAssignTagsCommand();
            }
        });

        addMenuListItem(new WebAdminImageButtonDefinition<Void, VM>(constants.guideMeVm(), IconType.SUPPORT, true) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getGuideCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Void, VM> getNewButtonDefinition() {
        return newButtonDefinition;
    }

}
