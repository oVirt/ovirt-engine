package org.ovirt.engine.core.dao.provider;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface ProviderDao extends GenericDao<Provider, Guid> {

    /**
     * Query for the provider by name.
     *
     * @param name
     *            The name of the provider.
     * @return The provider, or <code>null</code> if not found.
     */
    Provider getByName(String name);
}
