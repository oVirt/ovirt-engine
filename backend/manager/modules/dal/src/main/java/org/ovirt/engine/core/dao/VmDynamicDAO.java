package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.compat.Guid;

public interface VmDynamicDAO extends GenericDao<VmDynamic, Guid>, StatusAwareDao<Guid, VMStatus>,
        MassOperationsDao<VmDynamic, Guid> {
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
    @Override
    VmDynamic get(Guid id);

    /**
     * Updates the specified dynamic vm.
     *
     * @param vm
     *            the vm
     */
    @Override
    void update(VmDynamic vm);

    /**
     * Removes the specified dynamic vm.
     *
     * @param vmDynamic
     *            the vm
     */
    @Override
    void remove(Guid vm);

    /**
     * Saves the specified dynamic vm.
     *
     * @param vm
     *            the vm
     */
    @Override
    void save(VmDynamic vm);

    /**
     * Update the console user name and id, but only if it was empty before. This
     * method is needed in order to implement optimistic locking for the functionality
     * that allows reconnection to the console without rebooting the virtual machine.
     *
     * @param vm the dynamic data of the virtual machine
     * @return <code>true</true> if at least one row was updated, <code>false</code>
     *   otherwise
     */
    boolean updateConsoleUserWithOptimisticLocking(VmDynamic vm);
}
