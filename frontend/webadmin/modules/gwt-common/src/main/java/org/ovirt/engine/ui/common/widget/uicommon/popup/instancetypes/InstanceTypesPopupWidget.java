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
                update(foremanTab, hiddenField()).
                putAll(poolSpecificFields(), hiddenField()).
                putOne(isDeleteProtectedEditor, hiddenField()).
                putOne(isSealedEditor, hiddenField()).
                putOne(isStatelessEditor, hiddenField()).
                putOne(isRunAndPauseEditor, hiddenField()).
                putOne(commentEditor, hiddenField()).
                putOne(vmTypeEditor, hiddenField()).
                putOne(oSTypeEditor, hiddenField()).
                putOne(initialRunTab, hiddenField()).
                putOne(expander, hiddenField()).
                putOne(allowConsoleReconnectEditor, hiddenField()).
                putOne(cdAttachedEditor, hiddenField()).
                putOne(cdImageEditor, hiddenField()).
                putOne(refreshButton, hiddenField()).
                putOne(timeZoneEditor, hiddenField()).
                putOne(generalLabel, hiddenField()).
                putOne(quotaEditor, hiddenField()).
                putOne(cpuAllocationPanel, hiddenField()).
                putOne(vncKeyboardLayoutEditor, hiddenField()).
                putOne(storageAllocationPanel, hiddenField()).
                putOne(customPropertiesTab, hiddenField()).
                putOne(ssoMethodLabel, hiddenField()).
                putOne(ssoMethodNone, hiddenField()).
                putOne(ssoMethodGuestAgent, hiddenField()).
                putOne(hostCpuEditor, hiddenField()).
                putOne(tscFrequencyEditor, hiddenField()).
                putOne(templateVersionNameEditor, hiddenField()).
                putOne(bootMenuEnabledEditor, hiddenField()).
                putOne(serialNumberPolicyEditor, hiddenField()).
                putOne(timeZoneEditorWithInfo, hiddenField()).
                putOne(startRunningOnPanel, hiddenField()).
                putOne(spiceCopyPasteEnabledEditor, hiddenField()).
                putOne(spiceFileTransferEnabledEditor, hiddenField()).
                putOne(iconTab, hiddenField()).
                putOne(consoleDisconnectActionEditor, hiddenField()).
                putOne(resumeBehavior, hiddenField()).
                putOne(customCompatibilityVersionEditor, hiddenField()).
                update(affinityTab, hiddenField()).
                putOne(profilesInstanceTypeEditor, hiddenField());
    }

    @Override
    public void edit(UnitVmModel model) {
        super.edit(model);
        multiQueues.setEnabled(false);
    }
}
