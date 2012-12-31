package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VmNetworkStatisticsDAO</code> defines a type for performing CRUD operations on instances of
 * {@link VmNetworkStatistics}.
 */
public interface VmNetworkStatisticsDAO extends GenericDao<VmNetworkStatistics, Guid>,
        MassOperationsDao<VmNetworkStatistics, Guid> {
}
