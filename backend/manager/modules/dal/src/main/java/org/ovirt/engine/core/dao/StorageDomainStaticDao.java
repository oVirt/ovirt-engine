package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.compat.Guid;

public interface StorageDomainStaticDao extends GenericDao<StorageDomainStatic, Guid> {

    /**
     * Retrieves the instance with the specified name.
     *
     * @param name
     *            the domain name
     * @return the domain
     */
    StorageDomainStatic getByName(String name);

    /**
     * Retrieves the instance with the specified name.
     * @param name
     *            the domain name
     * @param userId
     *            the user id
     * @param filtered
     *            should the query use the user id
     * @return the domain
     */
    StorageDomainStatic getByName(String name, Guid userId, boolean filtered);

    /**
     * Retrieves all storage domains for the given storage pool.
     * @param pool
     *            the pool
     * @return the list of domains
     */
    List<StorageDomainStatic> getAllForStoragePool(Guid pool);

    /**
     * Return all the domains of the given status which belong to the given pool.
     *
     * @param pool
     *            The pool id.
     * @param status
     *            The desired status.
     * @return The domain ids list (empty if none satisfy the terms).
     */
    List<Guid> getAllIds(Guid pool, StorageDomainStatus status);
}
