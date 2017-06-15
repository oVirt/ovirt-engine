package org.ovirt.engine.ui.common.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * The Presenter Widget for the Role Permissions removal pop-up.
 */
public class RolePermissionsRemoveConfirmationPopupPresenterWidget extends
    AbstractModelBoundPopupPresenterWidget<ConfirmationModel,
    RolePermissionsRemoveConfirmationPopupPresenterWidget.ViewDef> {

    /**
     * The view definition interface.
     */
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ConfirmationModel> {
    }

    /**
     * Constructor.
     * @param eventBus The GWT event bus.
     * @param view The view.
     */
    @Inject
    public RolePermissionsRemoveConfirmationPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
