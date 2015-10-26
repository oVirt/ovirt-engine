package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class VirtualMachineMainTabSelectedItems extends AbstractMainTabSelectedItems<VM>
        implements VirtualMachineSelectionChangeEvent.VirtualMachineSelectionChangeHandler{

    @Inject
    VirtualMachineMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(VirtualMachineSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onVirtualMachineSelectionChange(VirtualMachineSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }

}
