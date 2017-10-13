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

public class VirtualMachineActionPanelPresenterWidget extends ActionPanelPresenterWidget<VM, VmListModel<Void>> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<VM> newButtonDefinition;

    @Inject
    public VirtualMachineActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<VM> view,
            MainModelProvider<VM, VmListModel<Void>> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<VM>(constants.newVm()) {

            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewVmCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.restoreVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getImportVmCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<VM>(constants.editVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<VM>(constants.removeVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.cloneVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneVmCommand();
            }
        });

        List<ActionButtonDefinition<VM>> runSubActions = new LinkedList<>();
        runSubActions.add(new UiCommandButtonDefinition<VM>(getSharedEventBus(), constants.runOnceVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRunOnceCommand();
            }
        });
        addComboActionButton(new WebAdminImageButtonDefinition<VM>(constants.runVm(), IconType.PLAY) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRunCommand();
            }
        }, runSubActions);
        addActionButton(new WebAdminImageButtonDefinition<VM>(constants.suspendVm(), IconType.MOON_O) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPauseCommand();
            }
        });

        List<ActionButtonDefinition<VM>> shutdownSubActions = new LinkedList<>();
        shutdownSubActions.add(new WebAdminImageButtonDefinition<VM>(constants.powerOffVm(), IconType.POWER_OFF) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getStopCommand();
            }
        });
        addComboActionButton(new WebAdminImageButtonDefinition<VM>(constants.shutDownVm(), IconType.STOP
                ) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getShutdownCommand();
            }
        }, shutdownSubActions);

        addActionButton(new WebAdminImageButtonDefinition<VM>(constants.rebootVm(), IconType.REPEAT) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRebootCommand();
            }
        });

        List<ActionButtonDefinition<VM>> consoleOptionsSubActions = new LinkedList<>();
        consoleOptionsSubActions.add(new UiCommandButtonDefinition<VM>(getSharedEventBus(),
                constants.consoleOptions()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditConsoleCommand();
            }
        });

        addComboActionButton(new WebAdminImageButtonDefinition<VM>(constants.consoleVm(), IconType.DESKTOP) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getConsoleConnectCommand();
            }
        }, consoleOptionsSubActions);

        addActionButton(new WebAdminButtonDefinition<VM>(constants.migrateVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getMigrateCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<VM>(constants.createSnapshotVM()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCreateSnapshotCommand();
            }
        });

        addDividerToKebab();
        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.changeCdVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getChangeCdCommand();
            }
        });
        addDividerToKebab();

        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.cancelMigrationVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCancelMigrateCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.cancelConvertVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCancelConvertCommand();
            }
        });
        addDividerToKebab();
        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.makeTemplateVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewTemplateCommand();
            }
        });
        addDividerToKebab();
        // Management operations drop down
        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.exportVmToExportDomain()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getExportCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.exportVmToOva()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getExportOvaCommand();
            }
        });
        addDividerToKebab();
        addMenuListItem(new WebAdminButtonDefinition<VM>(constants.assignTagsVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAssignTagsCommand();
            }
        });

        addMenuListItem(new WebAdminImageButtonDefinition<VM>(constants.guideMeVm(), IconType.SUPPORT, true) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getGuideCommand();
            }
        });
    }

    public WebAdminButtonDefinition<VM> getNewButtonDefinition() {
        return newButtonDefinition;
    }

}
