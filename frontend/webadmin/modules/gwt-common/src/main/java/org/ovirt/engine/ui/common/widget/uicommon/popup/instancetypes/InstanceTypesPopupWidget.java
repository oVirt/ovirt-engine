package org.ovirt.engine.ui.common.widget.uicommon.popup.instancetypes;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class InstanceTypesPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<InstanceTypesPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected void initialize() {
        super.initialize();

        mainTabPanel.setHeaderVisible(false);
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                putAll(poolSpecificFields(), hiddenField()).
                // Header
                putOne(templateVersionNameEditor, hiddenField()).
                putOne(oSTypeEditor, hiddenField()).
                putOne(vmTypeEditor, hiddenField()).
                putOne(quotaEditor, hiddenField()).
                putOne(biosTypeEditor, hiddenField()).
                // General
                putOne(commentEditor, hiddenField()).
                putOne(isDeleteProtectedEditor, hiddenField()).
                putOne(isSealedEditor, hiddenField()).
                putOne(isStatelessEditor, hiddenField()).
                putOne(isRunAndPauseEditor, hiddenField()).
                putOne(profilesInstanceTypeEditor, hiddenField()).
                // System
                putOne(timeZoneEditor, hiddenField()).
                putOne(timeZoneEditorWithInfo, hiddenField()).
                putOne(serialNumberPolicyEditor, hiddenField()).
                putOne(customSerialNumberEditor, hiddenField()).
                putOne(generalLabel, hiddenField()).
                putOne(customCompatibilityVersionEditor, hiddenField()).
                // Console
                putOne(spiceCopyPasteEnabledEditor, hiddenField()).
                putOne(spiceFileTransferEnabledEditor, hiddenField()).
                putOne(vncKeyboardLayoutEditor, hiddenField()).
                putOne(allowConsoleReconnectEditor, hiddenField()).
                putOne(consoleDisconnectActionEditor, hiddenField()).
                putOne(consoleDisconnectActionDelayEditor, hiddenField()).
                putOne(ssoMethodLabel, hiddenField()).
                putOne(ssoMethodNone, hiddenField()).
                putOne(ssoMethodGuestAgent, hiddenField()).
                putOne(expander, hiddenField()).
                // Initial Run
                putOne(initialRunTab, hiddenField()).
                // Host
                putOne(startRunningOnPanel, hiddenField()).
                putOne(hostCpuEditorPanel, hiddenField()).
                putOne(migrateEncryptedEditor, hiddenField()).
                putOne(migrateCompressedEditor, hiddenField()).
                putOne(autoConvergeEditor, hiddenField()).
                putOne(parallelMigrationsTypeEditor, hiddenField()).
                putOne(parallelMigrationsLabel, hiddenField()).
                putOne(parallelMigrationsInfoIcon, hiddenField()).
                putOne(customParallelMigrationsEditor, hiddenField()).
                // Highly Available
                putOne(leaseRow, hiddenField()).
                putOne(resumeBehaviorRow, hiddenField()).
                // Resource Allocation
                putOne(cpuAllocationPanel, hiddenField()).
                putOne(storageAllocationPanel, hiddenField()).
                putOne(trustedPlatformPanel, hiddenField()).
                // Boot
                putOne(bootMenuEnabledEditor, hiddenField()).
                putOne(cdAttachedEditor, hiddenField()).
                putOne(cdImageEditor, hiddenField()).
                putOne(refreshButton, hiddenField()).
                // Foreman
                update(foremanTab, hiddenField()).
                // Custom properties
                putOne(customPropertiesTab, hiddenField()).
                // Affinity
                update(affinityTab, hiddenField()).
                // Icon
                putOne(iconTab, hiddenField());
    }

    @Override
    public void edit(UnitVmModel model) {
        super.edit(model);
        multiQueues.setEnabled(false);
    }
}
