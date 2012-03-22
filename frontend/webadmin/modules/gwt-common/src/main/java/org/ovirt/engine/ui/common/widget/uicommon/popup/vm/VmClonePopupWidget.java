package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class VmClonePopupWidget extends AbstractVmPopupWidget {

    public VmClonePopupWidget(CommonApplicationConstants constants) {
        super(constants);
    }

    @Override
    public void edit(UnitVmModel object) {
        super.edit(object);
        initTabAvailabilityListeners(object);
    }

    private void initTabAvailabilityListeners(final UnitVmModel vm) {
        // TODO should be handled by the core framework
        vm.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsHostAvailable".equals(propName)) {
                    hostTab.setVisible(vm.getIsHostAvailable());
                } else if ("IsHostTabValid".equals(propName)) {
                    if (vm.getIsHostTabValid()) {
                        hostTab.markAsValid();
                    } else {
                        hostTab.markAsInvalid(null);
                    }
                }
            }
        });

        boolean isDesktop = vm.getVmType().equals(VmType.Desktop);

        // High Availability only available in server mode
        highAvailabilityTab.setVisible(!isDesktop);

        // only available for desktop mode
        isStatelessEditor.setVisible(isDesktop);
        numOfMonitorsEditor.setVisible(isDesktop);
    }

}
