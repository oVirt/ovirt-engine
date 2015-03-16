package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * This class is a representation of the remove permission popup view.
 */
public class RolePermissionsRemoveConfirmationPopupView extends RemoveConfirmationPopupView implements
    RolePermissionsRemoveConfirmationPopupPresenterWidget.ViewDef {

    /**
     * Constructor.
     * @param eventBus The GWT event bus.
     * @param resources The application resources.
     * @param messages The application messages.
     * @param constants The application constants.
     */
    @Inject
    public RolePermissionsRemoveConfirmationPopupView(EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationMessages messages,
            CommonApplicationConstants constants) {
        super(eventBus, resources, messages, constants);
        itemPanel.setHeight("80%"); //$NON-NLS-1$
    }

    @Override
    protected void addItemText(Object item) {
        // We assume that the objects passed in are of type permissions.
        Permission permissions = (Permission) item;
        addItemLabel(messages.roleOnUser(permissions.getRoleName(), permissions.getOwnerName()));
    }
}
