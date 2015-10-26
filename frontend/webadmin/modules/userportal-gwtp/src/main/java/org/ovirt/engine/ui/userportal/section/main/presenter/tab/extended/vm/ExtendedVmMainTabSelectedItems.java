package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.ExtendedVirtualMachineSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ExtendedVmMainTabSelectedItems extends AbstractMainTabSelectedItems<UserPortalItemModel>
    implements ExtendedVirtualMachineSelectionChangeEvent.ExtendedVirtualMachineSelectionChangeHandler {
    @Inject
    ExtendedVmMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(ExtendedVirtualMachineSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onExtendedVirtualMachineSelectionChange(ExtendedVirtualMachineSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
