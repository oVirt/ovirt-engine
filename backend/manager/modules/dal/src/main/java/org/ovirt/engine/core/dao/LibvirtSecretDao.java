package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code LibvirtSecretDao} defines a type which performs CRUD operations on instances of {@link LibvirtSecret}.
 */
public interface LibvirtSecretDao extends GenericDao<LibvirtSecret, Guid> {

    /**
     * Retrieves all secrets for the specified provider id.
     *
     * @param providerId
     *            The provider id
     * @return the list of secrets
     */
    List<LibvirtSecret> getAllByProviderId(Guid providerId);

    /**
     * Retrieves all secrets for the specified storage pool id and belong to an active storage domain.
     *
     * @param storagePoolId
     *            The storage pool id
     * @return the list of secrets
     */
    List<LibvirtSecret> getAllByStoragePoolIdFilteredByActiveStorageDomains(Guid storagePoolId);
}
