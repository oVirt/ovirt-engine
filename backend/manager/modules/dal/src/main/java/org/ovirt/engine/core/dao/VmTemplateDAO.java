package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmTemplateDAO</code> defines a type for performing CRUD operations on instances of {@link VmTemplate}.
 */
public interface VmTemplateDAO extends GenericDao<VmTemplate, Guid>, StatusAwareDao<Guid, VmTemplateStatus>, SearchDAO<VmTemplate> {

    /**
    * Retrieves the template with the given id with optional filtering.
    *
    * @param id
    *            The id to look by (can't be <code>null</code>).
    * @param userID
    *            the ID of the user requesting the information
    * @param isFiltered
    *            Whether the results should be filtered according to the user's permissions
    * @return The entity instance, or <code>null</code> if not found.
    */
    public VmTemplate get(Guid id, Guid userID, boolean isFiltered);

    /**
    * Retrieves the template with the given id with optional filtering.
    *
    * @param name
    *            The name to look by (can't be <code>null</code>).
    * @param userID
    *            the ID of the user requesting the information
    * @param isFiltered
    *            Whether the results should be filtered according to the user's permissions
    * @return The entity instance, or <code>null</code> if not found.
    */
    public VmTemplate getByName(String name, Guid userID, boolean isFiltered);

    /**
     * Retrieves all templates with optional filtering.
     *
     * @param userID
     *            the ID of the user requesting the information
     * @param isFiltered
     *            Whether the results should be filtered according to the user's permissions
     * @return
     */
    public List<VmTemplate> getAll(Guid userID, boolean isFiltered);

    /**
     * Retrieves all templates for the specified storage domain.
     *
     * @param storageDomain
     *            the storage domain
     * @return the list of templates
     */
    List<VmTemplate> getAllForStorageDomain(Guid storageDomain);

    /**
     * Retrieves all templates for the specified VDS group.
     *
     * @param vdsGroup
     *            the VDS group
     * @return the list of templates
     */
    List<VmTemplate> getAllForVdsGroup(Guid vdsGroup);

    /**
    * Retrieves all templates for the specified storage domain with optional filtering.
    *
    * @param storageDomain
    *            the storage domain
    * @param userID
    *            the ID of the user requesting the information
    * @param isFiltered
    *            Whether the results should be filtered according to the user's permissions
    * @return the list of templates
    */
    List<VmTemplate> getAllForStorageDomain(Guid storageDomain, Guid userID, boolean isFiltered);

    /**
     * Retrieves all templates related to quota id.
     *
     * @param quotaId
     *            the quota id
     * @return the list of templates
     */
    List<VmTemplate> getAllTemplatesRelatedToQuotaId(Guid quotaId);


    /**
     * Get all vm templates with the given ids
     *
     * @param templatesIds
     * @return
     */
    public List<VmTemplate> getVmTemplatesByIds(List<Guid> templatesIds);

    /**
     * Retrieves templates with permissions to perform the given action.
     *
     * @param userId
     * @param actionGroup
     * @return list of templates
     */
    List<VmTemplate> getTemplatesWithPermittedAction(Guid userId, ActionGroup actionGroup);

    /**
     * Retrieves the template with the specified image id.
     *
     * @param image
     *            the image id
     * @return A {@link Map} from the image's plug status to a {@link List} of the template associated with it.
     */
    Map<Boolean, VmTemplate> getAllForImage(Guid imageId);

    /**
     * Retrieves all VmTemplates that have a Network Interface that the given Network is attached to.
     *
     * @param networkId
     *            the network id
     * @return the list of VmTemplates
     */
    List<VmTemplate> getAllForNetwork(Guid networkId);
}
