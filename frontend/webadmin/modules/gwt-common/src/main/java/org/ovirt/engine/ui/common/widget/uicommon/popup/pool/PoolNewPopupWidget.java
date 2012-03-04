package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class PoolNewPopupWidget extends AbstractVmPopupWidget {

    public PoolNewPopupWidget(CommonApplicationConstants constants) {
        super(constants);
    }

    // TODO: Localize
    @Override
    protected void localize(CommonApplicationConstants constants) {
        super.localize(constants);
        dontMigrateVMEditor.setLabel("Do not migrate VM");
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);
        initTabAvailabilityListeners(object);
        isStatelessEditor.setVisible(false);
        storageAllocationPanel.setVisible(false);

        if (object.getIsNew()) {
            addVmsButton.setVisible(false);
            object.getAssignedVms().setIsAvailable(false);
            numOfDesktopsEditor.setLabel("Number of VMs");
        } else {
            object.getAssignedVms().setIsAvailable(true);
            numOfDesktopsEditor.setVisible(false);
            numOfDesktopsEditor.setLabel("Number of VMs to add");
            addVmsButton.setText("Add VMs");
            addVmsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addVmsButton.setVisible(false);
                    numOfDesktopsEditor.setVisible(true);
                    object.setIsAddVMMode(true);
                }
            });
        }
    }

    private void initTabAvailabilityListeners(final UnitVmModel pool) {
        // TODO should be handled by the core framework
        pool.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsPoolTabValid".equals(propName)) {
                    poolTab.markAsInvalid(null);
                }
            }
        });

        poolTab.setVisible(true);
    }

}
