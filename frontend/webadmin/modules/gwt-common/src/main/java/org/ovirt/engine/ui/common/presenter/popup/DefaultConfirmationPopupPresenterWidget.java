package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Default popup presenter widget for handling UiCommon {@link ConfirmationModel} dialogs.
 */
public class DefaultConfirmationPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ConfirmationModel, DefaultConfirmationPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ConfirmationModel> {
    }

    @Inject
    public DefaultConfirmationPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
