package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class PoolEditPopupWidget extends PoolNewPopupWidget {

    public PoolEditPopupWidget(CommonApplicationConstants constants) {
        super(constants);
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (object.getProgress() == null) { //$NON-NLS-1$
                    disableAllTabs();
                    enableEditPoolFields();
                }
            }
        });
    }

    private void enableEditPoolFields() {
        descriptionEditor.setEnabled(true);
        numOfDesktopsEditor.setEnabled(true);
        prestartedVmsEditor.setEnabled(true);
        quotaEditor.setEnabled(true);
    }

    private void disableAllTabs() {
        generalTab.disableContent();
        poolTab.disableContent();
        initialRunTab.disableContent();
        consoleTab.disableContent();
        hostTab.disableContent();
        highAvailabilityTab.disableContent();
        resourceAllocationTab.disableContent();
        bootOptionsTab.disableContent();
        customPropertiesTab.disableContent();
    }
}
