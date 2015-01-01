package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmDAO</code> defines a type for performing CRUD operations on instances of {@link VM}.
 *
 *
 */
public interface VmDAO extends DAO {
    /**
     * Returns the VM with the specified id.
     *
     * @param id
     *            the VM id
     * @return the VM
     */
    VM get(Guid id);

    /**
     * Returns the VM with the specified id, with optional filtering.
     *
     * @param id
     *            the VM id
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the VM
     */
    VM get(Guid id, Guid userID, boolean isFiltered);

    /**
     * Returns the VM with the specified name, with optional filtering.
     *
     * @param id
     *            the VM name
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return the VM
     */
    VM getByNameForDataCenter(Guid dataCenterId, String name, Guid userID, boolean isFiltered);

    /**
     * Retrieves the VMs with the specified image id.
     *
     *
     * @param disk
     *            the disk id
     * @param includeVmsSnapshotAttachedTo
     * @return A {@link Map} from the image's plug status to a {@link List} of the VMs associated with it.
     */
    Map<Boolean, List<VM>> getForDisk(Guid disk, boolean includeVmsSnapshotAttachedTo);

    /**
     * Retrieves a list of VMs for the specified disk id.
     *
     * @return A {@link List} of the VMs associated with the disk.
     */
    List<VM> getVmsListForDisk(Guid id, boolean includeVmsSnapshotAttachedTo);

    /**
     * Retrieves a list of VMs for the specified instance type id.
     *
     * @return A {@link List} of the VMs connected to the given instance type.
     */
    List<VM> getVmsListByInstanceType(Guid id);

    /**
     * Retrieves a list of VMs for the specified disk id.
     *
     * @param disk
     *            the disk id
     * @return A {@link List} of the VMs associated with the disk.
     */
    List<Pair<VM, VmDevice>> getVmsWithPlugInfo(Guid id);

    /**
     * Finds all VMs for the specified user.
     *
     * @param user
     *            ' the user id
     * @return the list of VMs
     */
    List<VM> getAllForUser(Guid user);

    /**
     * Retrieves the list of VMS for the given user.
     *
     * @param user
     *            the user id
     * @return the list of VMs
     */
    List<VM> getAllForUserWithGroupsAndUserRoles(Guid user);

    /**
     * Retrieves all VMs for the specified ad group name.
     *
     * @param name
     *            the ad group name
     * @return the list of VMs
     */
    List<VM> getAllForAdGroupByName(String name);

    /**
     * Retrieves all virtual machines associated with the given template.
     *
     * @param template
     *            the template id
     * @return the list of VMs
     */
    List<VM> getAllWithTemplate(Guid template);

    /**
     * Gets the list of virtual machines running on the specified VFDS instance.
     *
     * @param vds
     *            the VDS id
     * @return the list of VMs
     */
    List<VM> getAllRunningForVds(Guid vds);

    /**
     * Gets the list of virtual machines running on or migrating to the specified VDS instance.
     *
     * @param vds
     *            the VDS id
     * @return the list of VMs
     */
    List<VM> getAllRunningOnOrMigratingToVds(Guid vds);

    /**
     * Returns the list of virtual machines running on the specified VDS instance.
     *
     * @param vds
     *            the VDS id
     * @return the list of VMs
     */
    Map<Guid, VM> getAllRunningByVds(Guid vds);

    /**
     * Finds the list of VMs using the supplied query.
     *
     * @param query
     *            the SQL query
     * @return the list of VMs
     */
    List<VM> getAllUsingQuery(String query);

    /**
     * Retrieves the list of VMs for the given storage domain.
     *
     * @param storageDomain
     *            the storage domain id
     * @return the list of VMs
     */
    List<VM> getAllForStorageDomain(Guid storageDomain);

    /**
     * Retrieves all running VMs that require the given storage domain to be active.
     * In other words, this method returns a list of VMs that have plugged disks that reside
     * on the given storage domain.
     *
     * @param storageDomain
     *            the storage domain's ID
     * @return the running VMs
     */
    List<VM> getAllActiveForStorageDomain(Guid storageDomain);

    /**
     * Get all vms related to quota id
     *
     * @param quotaId
     * @return
     */
    public List<VM> getAllVmsRelatedToQuotaId(Guid quotaId);


    /**
     * Get all vms with the given ids
     *
     * @param vmsIds
     * @return
     */
    public List<VM> getVmsByIds(List<Guid> vmsIds);

    /**
     * Retrieves the list of all VMS with optional permission filtering.
     *
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     *
     * @return the list of all VMs
     */
    List<VM> getAll(Guid userID, boolean isFiltered);

    /**
     * Retrieves the list of all VMS.
     *
     * @return the list of all VMs
     */
    List<VM> getAll();

    /**
     * Saves the is_initialized property of the VM.
     *
     * @param vmid
     *            The VM's ID
     * @param isInitialized
     *            Whether or not the VM is initialized.
     */
    void saveIsInitialized(Guid vmid, boolean isInitialized);
    /**
     * Removes the VM with the specified id.
     *
     * @param vm
     *            the VM id
     */
    void remove(Guid vm);

    /**
     * Retrieves all VMS that have a Network Interface that the given Network is attached to.
     *
     * @param networkId
     *            the network id
     * @return the list of VMs
     */
    List<VM> getAllForNetwork(Guid networkId);

    /**
     * Retrieves all VMS that belong to the provided vds group
     * @param vds_group_id
     * @return
     */
    List<VM> getAllForVdsGroup(Guid vds_group_id);

    /**
     * Retrieves all VMS that belong to the provided vm pool
     * @param vm_pool_id
     *             the pool id
     * @return
     */
    List<VM> getAllForVmPool(Guid vmPoolId);

    /**
     * Retrieves all VMS that are migrating to a certain Host
     * @param vdsId
     *            The Host id
     */
    List<VM> getAllMigratingToHost(Guid vdsId);

    /**
     * Retrieves all VMS that have a Network Interface that the given Profile is attached to.
     *
     * @param vnicProfileId
     *            the vm network interface profile id
     * @return the list of VMs
     */
    List<VM> getAllForVnicProfile(Guid vnicProfileId);

    /**
     * Updates the original template name property of all the VMs which are based on template with id originalTemplateId
     *
     * @param originalTemplateId the template on which the VM has been based on (thin or clone)
     * @param originalTemplateName the new name of the template on which the VM has been based on
     */
    void updateOriginalTemplateName(Guid originalTemplateId, String originalTemplateName);

    /**
     * Retrieves all auto started VMs that went down unintentionally
     *
     * @return the list of VMs
     */
    List<VM> getAllFailedAutoStartVms();

    /**
     * get all running VMs under specified cluster
     *
     * @param clusterId
     * @return
     */
    List<VM> getAllRunningByCluster(Guid clusterId);

    /**
     * Retrieves all VM names which contains disks on other Storage Domain other then the storageDomain GUID.
     *
     * @param storageDomainGuid
     *            the storage domain GUID
     * @return List of VMs
     */
    List<VM> getAllVMsWithDisksOnOtherStorageDomain(Guid storageDomainGuid);

    /**
     * Retrieves all ids of vms that are candidate for version update for this base template:
     * template_version_number is set to null
     * status is down
     * vm is stateless or belong to vm pool
     * (for vm in pool, check there is no stateless snapshot for it [manual pool])
     *
     * @return the list of ids of these vms
     */
    List<Guid> getVmIdsForVersionUpdate(Guid baseTemplateId);

    /**
     * Retrieves all vms given storage pool (Data Center) id
     *
     * @param storagePoolId id for storage pool
     * @return vms that are part of the given storage pool
     */
    List<VM> getAllForStoragePool(Guid storagePoolId);
}
