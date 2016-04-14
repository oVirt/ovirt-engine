package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.compat.Guid;

public interface VmJobDao extends GenericDao<VmJob, Guid>, MassOperationsDao<VmJob, Guid> {

}
