package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
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
     * Retrieves the VM for the specified hibernate image.
     *
     * @param hibernationImage
     *            the hibernation image
     * @return the VM
     */
    VM getForHibernationImage(Guid hibernationImage);

    /**
     * Retrieves the VMs with the specified image id.
     *
     * @param disk
     *            the disk id
     * @return A {@link Map} from the image's plug status to a {@link List} of the VMs associated with it.
     */
    Map<Boolean, List<VM>> getForDisk(Guid disk);

    /**
     * Retrieves a list of VMs fir the specified disk id.
     *
     * @param disk
     *            the disk id
     * @return A {@link List} of the VMs associated with the disk.
     */
    List<VM> getVmsListForDisk(Guid id);

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
     * Finds those VMs associated with a specific power client by VDS.
     *
     * @param vds
     *            the VDS id
     * @return the list of VMs
     */
    List<VM> getAllForDedicatedPowerClientByVds(Guid vds);

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
     * Retrieves all running VMs for the given storage domain.
     *
     * @param storageDomain
     *            the storage domain
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
     * Saves the supplied VM.
     *
     * @param vm
     *            the VM
     */
    void save(VM vm);

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
}
