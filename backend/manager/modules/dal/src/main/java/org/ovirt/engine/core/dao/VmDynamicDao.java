package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface VmDynamicDao extends GenericDao<VmDynamic, Guid>, StatusAwareDao<Guid, VMStatus>,
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
     * Check whether or not any VM runs on the given VDS instance.
     *
     * @param vdsId
     *            the VDS id
     * @return false if no VM runs on the host, true otherwise
     */
    boolean isAnyVmRunOnVds(Guid vdsId);

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
     * Get all VmDynamic with the given ids
     * @param vmIds
     *            the list of VM ids
     * @return list of corresponding dynamics
     */
    List<VmDynamic> getByIds(List<Guid> vmIds);

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
     * @param vm
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

    void clearMigratingToVds(Guid id);

    void clearMigratingToVdsAndSetDynamicPinning(Guid id, String cpuPinning, String numaPinning);


    /**
     * Update vm dynamics ovirt_guest_agent_status field
     *
     * @param the vm id
     * @param new status
     */
    void updateOvirtGuestAgentStatus(Guid vmId, GuestAgentStatus ovirtGuestAgentStatus);

    /**
     * Update vm dynamics qemu_guest_agent_status field
     *
     * @param the vm id
     * @param new status
     */
    void updateQemuGuestAgentStatus(Guid vmId, GuestAgentStatus qemuGuestAgentStatus);

    /**
     * Get value of hash field for every VM in vm_dynamic table.
     */
    List<Pair<Guid, String>> getAllDevicesHashes();

    /**
     * Set value of hash field for every VM in the list in one batch.
     *
     * @param vmHashes list of (VM id, hash value) pairs
     */
    void updateDevicesHashes(List<Pair<Guid, String>> vmHashes);

    /**
     * Update the status of all the given VMs to unknown
     *
     * @param vmIds - IDs of VMs to update
     */
    void updateVmsToUnknown(List<Guid> vmIds);


    /**
     * Retrieves all that are migrating to a certain Host
     * @param vdsId
     *            The Host id
     */
    List<VmDynamic> getAllMigratingToHost(Guid vdsId);

    /**
     * Update the dynamic properties of VM lease.
     *
     * @param vmId the vm id
     * @param leaseInfo dynamic properties of the lease
     */
    void updateVmLeaseInfo(Guid vmId, Map<String, String> leaseInfo);


    /**
     * Retrieves all VM IDs with the specified ISO disk attached as a CDROM.
     *
     * @param isoDiskId
     *            the ISO disk ID
     * @return the VM IDs with with the specified ISO disk attached as a CDROM
     */
    List<Guid> getAllIdsWithSpecificIsoAttached(Guid isoDiskId);

    /**
     * Retrieve all running VMs for which the user with the specified role has direct or indirect
     * permissions on
     * @param userID
     *             the user id
     * @param actionGroup
     *             the actionGroup
     * @return the list of VmDynamics
     */
    List<VmDynamic> getAllRunningForUserAndActionGroup(Guid userID, ActionGroup actionGroup);
}
