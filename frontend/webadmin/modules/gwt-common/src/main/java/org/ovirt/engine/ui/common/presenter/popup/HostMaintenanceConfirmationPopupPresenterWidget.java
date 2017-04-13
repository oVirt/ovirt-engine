package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.HostMaintenanceConfirmationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements the host maintenance confirmation dialog bound to UiCommon {@link HostMaintenanceConfirmationModel}.
 */
public class HostMaintenanceConfirmationPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostMaintenanceConfirmationModel, HostMaintenanceConfirmationPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostMaintenanceConfirmationModel> {
        void updateReasonVisibility(HostMaintenanceConfirmationModel model);
    }

    @Inject
    public HostMaintenanceConfirmationPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final HostMaintenanceConfirmationModel model) {
        updateReasonVisibility(model);
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;

            if ("ReasonVisible".equals(propName)) { //$NON-NLS-1$
                updateReasonVisibility(model);
            }
        });
        super.init(model);
    }

    protected void updateReasonVisibility(HostMaintenanceConfirmationModel model) {
        getView().updateReasonVisibility(model);
    }

    @Override
    protected void updateHashName(HostMaintenanceConfirmationModel model) {
        super.updateHashName(model);

        // The message depends on the hash name
        updateMessage(model);
    }

}
