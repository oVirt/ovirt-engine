package org.ovirt.engine.core.dao.profiles;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface CpuProfileDao extends ProfilesDao<CpuProfile>, GenericDao<CpuProfile, Guid> {

    /**
     * Retrieves all CPU profiles associated with the given cluster id.
     *
     * @param clusterId
     *            the cluster's ID
     * @return the list of CPU profiles
     */
    List<CpuProfile> getAllForCluster(Guid clusterId);

    /**
     * Retrieves all CPU profiles associated with the given cluster id, according user's permissions.
     *
     * @param clusterId
     *            the cluster's ID
     * @param userId
     *            the user's ID
     * @param isFiltered
     *            indicating whether the results should be filtered according to the user's permissions
     * @param actionGroup
     *            The action group to filter by.
     *
     * @return the list of CPU profiles
     */
    List<CpuProfile> getAllForCluster(Guid clusterId, Guid userId, boolean isFiltered, ActionGroup actionGroup);

}
