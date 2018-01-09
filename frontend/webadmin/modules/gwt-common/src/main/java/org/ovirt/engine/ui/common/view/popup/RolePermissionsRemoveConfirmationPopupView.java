package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * This class is a representation of the remove permission popup view.
 */
public class RolePermissionsRemoveConfirmationPopupView extends RemoveConfirmationPopupView implements
    RolePermissionsRemoveConfirmationPopupPresenterWidget.ViewDef {

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    /**
     * Constructor.
     * @param eventBus The GWT event bus.
     */
    @Inject
    public RolePermissionsRemoveConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected void addItemText(Object item) {
        // We assume that the objects passed in are of type permissions.
        Permission permissions = (Permission) item;
        addItemLabel(messages.roleOnUser(permissions.getRoleName(), permissions.getOwnerName()));
    }
}
