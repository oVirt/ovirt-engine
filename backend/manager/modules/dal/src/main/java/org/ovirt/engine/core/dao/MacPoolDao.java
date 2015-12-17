package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.compat.Guid;

public interface MacPoolDao extends GenericDao<MacPool, Guid> {

    MacPool getDefaultPool();

    List<String> getAllMacsForMacPool(Guid macPoolId);

    MacPool getByClusterId(Guid id);
}
