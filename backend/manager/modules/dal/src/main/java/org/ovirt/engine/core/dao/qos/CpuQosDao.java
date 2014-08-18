package org.ovirt.engine.core.dao.qos;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.compat.Guid;

public interface CpuQosDao extends QosDao<CpuQos> {

    CpuQos getCpuQosByVmId(Guid vmId);

}
