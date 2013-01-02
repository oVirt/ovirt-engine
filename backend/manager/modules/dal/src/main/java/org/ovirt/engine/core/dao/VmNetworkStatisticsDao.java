package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmNetworkStatisticsDao</code> defines a type for performing CRUD operations on instances of
 * {@link VmNetworkStatistics}.
 */
public interface VmNetworkStatisticsDao extends GenericDao<VmNetworkStatistics, Guid>,
        MassOperationsDao<VmNetworkStatistics, Guid> {
}
