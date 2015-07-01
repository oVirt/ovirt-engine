package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;

public interface VmStatisticsDao extends GenericDao<VmStatistics, Guid>, MassOperationsDao<VmStatistics, Guid> {

}
