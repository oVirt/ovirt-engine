package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>VdsDAO</code> defines a type that performs CRUD operations on instances of {@link VDS}.
 *
 *
 */
public interface VdsDAO extends DAO {
    /**
     * Retrieves the instance with the given id.
     *
     * @param id
     *            the id
     * @return the VDS instance
     */
    VDS get(NGuid id);

    /**
     * Finds all instances with the given name.
     *
     * @param name
     *            the name
     * @return the list of instances
     */
    List<VDS> getAllWithName(String name);

    /**
     * Finds all instances for the given host.
     *
     * @param hostname
     *            the hostname
     * @return the list of instances
     */
    List<VDS> getAllForHostname(String hostname);

    /**
     * Retrieves all instances with the given address.
     *
     * @param address
     *            the address
     * @return the list of instances
     */
    List<VDS> getAllWithIpAddress(String address);

    /**
     * Retrieves all instances with the given unique id.
     *
     * @param id
     *            the unique id
     * @return the list of instances
     */
    List<VDS> getAllWithUniqueId(String id);

    /**
     * Retrieves all instances for the specified type.
     *
     * @param vds
     *            the type
     * @return the list of instances
     */
    List<VDS> getAllOfType(VDSType vds);

    /**
     * Retrieves all instances for the given list of types.
     *
     * @param types
     *            the type filter
     * @return the list of instances
     */
    List<VDS> getAllOfTypes(VDSType[] types);

    /**
     * Retrieves all instances by group id.
     *
     * @param vdsGroup
     *            the group id
     * @return the list of instances
     */
    List<VDS> getAllForVdsGroupWithoutMigrating(Guid vdsGroup);

    /**
     * Retrieves all instances using the supplied SQL query.
     *
     * @param query
     *            the query
     * @return the list of instances
     */
    List<VDS> getAllWithQuery(String query);

    /**
     * Retrieves all VDS instances.
     *
     * @return the list of VDS instances
     */
    List<VDS> getAll();

    /**
     * Retrieves all VDS instances by vds group id (cluster ID)
     *
     * @param vdsGroup
     * @return the list of VDS instances
     */
    List<VDS> getAllForVdsGroup(Guid vdsGroup);

}
