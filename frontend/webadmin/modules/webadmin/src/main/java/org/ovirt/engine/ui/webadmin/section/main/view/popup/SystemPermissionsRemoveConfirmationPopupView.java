package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.view.popup.RemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.SystemPermissionsRemoveConfirmationPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * This class is a representation of the remove system permission popup view.
 */
public class SystemPermissionsRemoveConfirmationPopupView extends RemoveConfirmationPopupView implements
    SystemPermissionsRemoveConfirmationPopupPresenterWidget.ViewDef {

    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public SystemPermissionsRemoveConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
        itemColumn.setHeight("80%"); //$NON-NLS-1$
    }

    @Override
    protected void addItemText(Object item) {
        // We assume that the objects passed in are of type permissions.
        Permission permissions = (Permission) item;
        addItemLabel(messages.userWithRole(permissions.getOwnerName(), permissions.getRoleName()));
    }
}
