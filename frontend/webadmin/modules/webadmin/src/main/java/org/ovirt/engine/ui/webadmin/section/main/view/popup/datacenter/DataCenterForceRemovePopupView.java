package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.common.view.popup.ForceRemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterForceRemovePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DataCenterForceRemovePopupView extends ForceRemoveConfirmationPopupView
        implements DataCenterForceRemovePopupPresenterWidget.ViewDef {

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public DataCenterForceRemovePopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected String getWarning() {
        return constants.dataCenterForceRemovePopupWarningLabel();
    }

    @Override
    protected String getFormattedMessage(String itemName) {
        return messages.detaCenterForceRemovePopupMessageLabel(itemName);
    }

}
