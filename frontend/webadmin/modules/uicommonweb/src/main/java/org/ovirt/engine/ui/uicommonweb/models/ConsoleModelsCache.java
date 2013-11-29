package org.ovirt.engine.ui.uicommonweb.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;

/**
 * Class that holds (caches) consoles for set of VM (typically contained in some ListModel).
 *
 * It provides a way to get consoles for given vm.
 * The cache must be periodically updated (when the set of VM is updated, the updateCache
 * method must be called to ensure the underlying models have fresh and correct instance
 * of their VM).
 *
 */
public class ConsoleModelsCache {

    private final Map<Guid, VmConsoles> vmConsoles;
    private final Model parentModel;
    private final ConsoleContext consoleContext;

    public ConsoleModelsCache(ConsoleContext consoleContext, Model parentModel) {
        vmConsoles = new HashMap<Guid, VmConsoles>();
        this.consoleContext = consoleContext;
        this.parentModel = parentModel;
    }

    /**
     * Gets consoles for given VM
     * @param vm
     * @return vm's consoles
     */
    public VmConsoles getVmConsolesForVm(VM vm) {
        return vmConsoles.get(vm.getId());
    }

    /**
     * This must be called every update cycle.
     * @param newItems
     */
    public void updateCache(Iterable<VM> newItems) {
        Set<Guid> vmIds = new HashSet<Guid>();

        if (newItems != null) {
            for (VM vm : newItems) {
                if (vmConsoles.containsKey(vm.getId())) {
                    vmConsoles.get(vm.getId()).setVm(vm); // only update vm
                } else {
                    vmConsoles.put(vm.getId(), new VmConsolesImpl(vm, parentModel, consoleContext));
                }
                vmIds.add(vm.getId());
            }
        }

        vmConsoles.keySet().retainAll(vmIds);
    }
}
