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
     * Retrieves all templates related to quota id.
     *
     * @param quotaId
     *            the quota id
     * @return the list of templates
     */
    List<VmTemplate> getAllTemplatesRelatedToQuotaId(Guid quotaId);

    /**
     * Retrieves templates with permissions to perform the given action.
     *
     * @param userId
     * @param actionGroup
     * @return list of templates
     */
    List<VmTemplate> getTemplatesWithPermittedAction(Guid userId, ActionGroup actionGroup);

    /**
     * Retrieves the templates with the specified image id.
     *
     * @param image
     *            the image id
     * @return A {@link Map} from the image's plug status to a {@link List} of the templates associated with it.
     */
    Map<Boolean, List<VmTemplate>> getAllForImage(Guid imageId);
}
