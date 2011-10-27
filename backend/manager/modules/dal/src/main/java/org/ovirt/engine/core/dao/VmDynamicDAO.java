package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.compat.Guid;

public interface VmDynamicDAO extends GenericDao<VmDynamic, Guid>, StatusAwareDao<Guid, VMStatus>,
        MassOperationsDao<VmDynamic> {
    /**
     * Retrieves all running dynamic VMs for the given VDS instance.
     *
     * @param vds
     *            the VDS id
     * @return the list of dynamic vms
     */
    List<VmDynamic> getAllRunningForVds(Guid vds);

    /**
     * Returns the dynamic VM with the specified id.
     *
     * @param id
     *            the id
     * @return the dynamic vm
     */
    VmDynamic get(Guid id);

    /**
     * Updates the specified dynamic vm.
     *
     * @param vm
     *            the vm
     */
    void update(VmDynamic vm);

    /**
     * Removes the specified dynamic vm.
     *
     * @param vmDynamic
     *            the vm
     */
    void remove(Guid vm);

    /**
     * Saves the specified dynamic vm.
     *
     * @param vm
     *            the vm
     */
    void save(VmDynamic vm);
}
