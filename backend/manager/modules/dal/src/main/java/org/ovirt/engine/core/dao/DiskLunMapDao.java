package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.DiskLunMapId;

public interface DiskLunMapDao extends GenericDao<DiskLunMap, DiskLunMapId> {

    DiskLunMap getDiskIdByLunId(String lunId);

}
