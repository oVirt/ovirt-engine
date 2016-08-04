package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.GuestContainer;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmGuestContainerListModel extends SearchableListModel<VM, GuestContainer> {

    public static final String HASH_NAME = "guest_containers"; //$NON-NLS-1$

    public VmGuestContainerListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().containersTitle());
        setHelpTag(HelpTag.guest_containers);
        setHashName(HASH_NAME);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);
        if (e.propertyName.equals("guestContainers")) { //$NON-NLS-1$
            updateGuestContainers();
        }
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        updateGuestContainers();
    }

    protected void updateGuestContainers() {
        if (getEntity() != null) {
            setItems(getEntity().getGuestContainers());
        }
    }

    @Override
    protected void syncSearch() {
        updateGuestContainers();
    }

    @Override
    protected String getListName() {
        return "VmGuestContainerListModel"; //$NON-NLS-1$
    }
}
