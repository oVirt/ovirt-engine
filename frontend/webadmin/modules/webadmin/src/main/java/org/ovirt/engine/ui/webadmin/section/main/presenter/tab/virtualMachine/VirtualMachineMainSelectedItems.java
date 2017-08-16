package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.VirtualMachineSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VirtualMachineMainSelectedItems extends AbstractMainSelectedItems<VM>
        implements VirtualMachineSelectionChangeEvent.VirtualMachineSelectionChangeHandler{

    @Inject
    VirtualMachineMainSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(VirtualMachineSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onVirtualMachineSelectionChange(VirtualMachineSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }

}
