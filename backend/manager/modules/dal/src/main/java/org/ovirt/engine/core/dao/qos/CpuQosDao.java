package org.ovirt.engine.core.dao.qos;

import java.util.Collection;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.compat.Guid;

public interface CpuQosDao extends QosDao<CpuQos> {

    Map<Guid, CpuQos> getCpuQosByVmIds(Collection<Guid> vmIds);
}
