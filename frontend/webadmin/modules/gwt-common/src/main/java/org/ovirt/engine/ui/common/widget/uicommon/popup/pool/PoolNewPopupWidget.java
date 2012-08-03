package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;

public class PoolNewPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<PoolNewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public PoolNewPopupWidget(CommonApplicationConstants constants) {
        super(constants);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
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

        numOfVmsLabel.setVisible(true);

        if (object.getIsNew()) {
            object.getNumOfDesktops().setEntity("1"); //$NON-NLS-1$
            assignedVmsEditor.setVisible(false);
            numOfDesktopsEditor.setLabel(""); //$NON-NLS-1$
            prestartedVmsEditor.setVisible(false);
            prestartedVmsHintLabel.setVisible(false);
        } else {
            assignedVmsEditor.addLabelStyleName(style.assignedVmsLabel());
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
