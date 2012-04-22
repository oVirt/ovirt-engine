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

    private CommonApplicationConstants constants;
    public PoolNewPopupWidget(CommonApplicationConstants constants) {
        super(constants);
        this.constants = constants;
    }

    @Override
    protected void localize(CommonApplicationConstants constants) {
        super.localize(constants);
        dontMigrateVMEditor.setLabel(constants.dontMigrageVmPoolPopup());
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);
        initTabAvailabilityListeners(object);
        isStatelessEditor.setVisible(false);

        if (object.getIsNew()) {
            addVmsButton.setVisible(false);
            object.getAssignedVms().setIsAvailable(false);
            numOfDesktopsEditor.setLabel(constants.numOfVmsPoolPopup());
        } else {
            object.getAssignedVms().setIsAvailable(true);
            numOfDesktopsEditor.setVisible(false);
            numOfDesktopsEditor.setLabel(constants.numOfVmsToAddPoolPopup());
            addVmsButton.setText(constants.addVmsPoolPopup());
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
                if ("IsPoolTabValid".equals(propName)) { //$NON-NLS-1$
                    poolTab.markAsInvalid(null);
                }
            }
        });

        poolTab.setVisible(true);
    }

}
