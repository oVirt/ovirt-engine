package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

/**
 * <code>LibvirtSecretDao</code> defines a type which performs CRUD operations on instances of {@link LibvirtSecret}.
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
}
