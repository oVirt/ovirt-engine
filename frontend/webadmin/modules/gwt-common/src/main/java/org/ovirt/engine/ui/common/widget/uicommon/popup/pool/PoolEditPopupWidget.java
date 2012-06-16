package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class PoolEditPopupWidget extends PoolNewPopupWidget {

    public PoolEditPopupWidget(CommonApplicationConstants constants) {
        super(constants);
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);
        disableAllTabs();
        enableEditPoolFields();
    }

    private void enableEditPoolFields() {
        descriptionEditor.setEnabled(true);
        numOfDesktopsEditor.setEnabled(true);
        prestartedVmsEditor.setEnabled(true);
    }

    private void disableAllTabs() {
        generalTab.disableContent();
        poolTab.disableContent();
        windowsSysPrepTab.disableContent();
        consoleTab.disableContent();
        hostTab.disableContent();
        highAvailabilityTab.disableContent();
        resourceAllocationTab.disableContent();
        bootOptionsTab.disableContent();
        customPropertiesTab.disableContent();
    }
}
