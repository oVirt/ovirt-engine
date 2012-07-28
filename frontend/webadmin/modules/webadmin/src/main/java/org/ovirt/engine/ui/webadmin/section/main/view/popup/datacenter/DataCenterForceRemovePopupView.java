package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.common.view.popup.ForceRemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterForceRemovePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DataCenterForceRemovePopupView extends ForceRemoveConfirmationPopupView
        implements DataCenterForceRemovePopupPresenterWidget.ViewDef {

    @Inject
    public DataCenterForceRemovePopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationMessages messages) {
        super(eventBus, resources, constants, messages);
    }

    @Override
    protected String getWarning() {
        return ((ApplicationConstants) constants).dataCenterForceRemovePopupWarningLabel();
    }

    @Override
    protected String getFormattedMessage(String itemName) {
        return ((ApplicationMessages) messages).detaCenterForceRemovePopupMessageLabel(itemName);
    }

}
