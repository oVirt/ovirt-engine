package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class VmDesktopNewPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<VmDesktopNewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public VmDesktopNewPopupWidget(CommonApplicationConstants constants, CommonApplicationResources resources, CommonApplicationMessages messages) {
        super(constants, resources, messages);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(UnitVmModel object) {
        super.edit(object);
        initTabAvailabilityListeners(object);

        if (object.isVmAttachedToPool())
            disableAllWidgetsExceptNameEditor();
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        // High Availability only avail in server mode
        highAvailabilityTab.setVisible(false);

        // only avail for desktop mode
        isStatelessEditor.setVisible(true);
        numOfMonitorsEditor.setVisible(true);
        allowConsoleReconnectEditor.setVisible(true);
        expander.setVisible(true);
    }

    private void disableAllWidgetsExceptNameEditor() {
        // ==General Tab==
        dataCenterEditor.setEnabled(false);
        clusterEditor.setEnabled(false);
        quotaEditor.setEnabled(false);
        descriptionEditor.setEnabled(false);

        numOfVmsEditor.setEnabled(false);
        prestartedVmsEditor.setEnabled(false);
        editPrestartedVmsEditor.setEnabled(false);

        templateEditor.setEnabled(false);
        memSizeEditor.setEnabled(false);
        totalvCPUsEditor.setEnabled(false);

        corePerSocketEditor.setEnabled(false);
        numOfSocketsEditor.setEnabled(false);

        oSTypeEditor.setEnabled(false);
        isStatelessEditor.setEnabled(false);
        isDeleteProtectedEditor.setEnabled(false);

        // ==Initial run Tab==
        timeZoneEditor.setEnabled(false);
        domainEditor.setEnabled(false);

        // ==Console Tab==
        displayProtocolEditor.setEnabled(false);
        usbSupportEditor.setEnabled(false);
        numOfMonitorsEditor.setEnabled(false);
        isSmartcardEnabledEditor.setEnabled(false);
        allowConsoleReconnectEditor.setEnabled(false);

        // ==Host Tab==
        isAutoAssignEditor.setEnabled(false);
        specificHost.setEnabled(false);
        defaultHostEditor.setEnabled(false);
        runVMOnSpecificHostEditor.setEnabled(false);
        dontMigrateVMEditor.setEnabled(false);
        cpuPinning.setEnabled(false);

        // ==Resource Allocation Tab==
        minAllocatedMemoryEditor.setEnabled(false);
        provisioningEditor.setEnabled(false);
        provisioningThinEditor.setEnabled(false);
        provisioningCloneEditor.setEnabled(false);
        disksAllocationView.setEnabled(false);

        // ==Boot Options Tab==
        firstBootDeviceEditor.setEnabled(false);
        secondBootDeviceEditor.setEnabled(false);
        cdAttachedEditor.setEnabled(false);
        cdImageEditor.setEnabled(false);
        kernel_pathEditor.setEnabled(false);
        initrd_pathEditor.setEnabled(false);
        kernel_parametersEditor.setEnabled(false);

        // ==Custom Properties Tab==
        customPropertiesSheetEditor.setEnabled(false);
    }
}
