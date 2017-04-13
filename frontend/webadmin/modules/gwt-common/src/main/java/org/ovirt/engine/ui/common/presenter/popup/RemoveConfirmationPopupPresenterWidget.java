package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements the remove confirmation dialog bound to UiCommon {@link ConfirmationModel}.
 */
public class RemoveConfirmationPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ConfirmationModel, RemoveConfirmationPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ConfirmationModel> {
        void updateReasonVisibility(ConfirmationModel model);
    }

    @Inject
    public RemoveConfirmationPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final ConfirmationModel model) {
        updateReasonVisibility(model);
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;

            if ("ReasonVisible".equals(propName)) { //$NON-NLS-1$
                updateReasonVisibility(model);
            }
        });
        super.init(model);
    }

    protected void updateReasonVisibility(ConfirmationModel model) {
        getView().updateReasonVisibility(model);
    }

    @Override
    protected void updateHashName(ConfirmationModel model) {
        super.updateHashName(model);

        // The message depends on the hash name
        updateMessage(model);
    }

}
