package org.ovirt.engine.core.dao.network;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.MassOperationsDao;

/**
 * <code>VmNetworkStatisticsDao</code> defines a type for performing CRUD operations on instances of
 * {@link VmNetworkStatistics}.
 */
public interface VmNetworkStatisticsDao extends GenericDao<VmNetworkStatistics, Guid>,
        MassOperationsDao<VmNetworkStatistics, Guid> {
}
