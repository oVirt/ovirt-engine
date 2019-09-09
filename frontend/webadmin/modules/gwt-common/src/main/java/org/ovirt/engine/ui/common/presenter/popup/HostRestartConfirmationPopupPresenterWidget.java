package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostRestartConfirmationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements the host restart confirmation dialog bound to UiCommon {@link HostRestartConfirmationModel}.
 */
public class HostRestartConfirmationPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostRestartConfirmationModel, HostRestartConfirmationPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostRestartConfirmationModel> {
    }

    @Inject
    public HostRestartConfirmationPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final HostRestartConfirmationModel model) {
        super.init(model);
    }

    @Override
    protected void updateHashName(HostRestartConfirmationModel model) {
        super.updateHashName(model);

        // The message depends on the hash name
        updateMessage(model);
    }

}
