package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.compat.Guid;

public interface VmInitDAO extends GenericDao<VmInit, Guid> {
    List<VmInit> getVmInitByIds(List<Guid> ids);
}

