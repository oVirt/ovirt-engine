package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.compat.Guid;

public interface MacPoolDao extends GenericDao<MacPool, Guid> {

    MacPool getDefaultPool();

    int getDcUsageCount(Guid id);

    List<String> getAllMacsForMacPool(Guid macPoolId);

    MacPool getByDataCenterId(Guid id);
}
