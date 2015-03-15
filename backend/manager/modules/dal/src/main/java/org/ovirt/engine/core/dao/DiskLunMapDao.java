package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMapId;

public interface DiskLunMapDao extends GenericDao<DiskLunMap, DiskLunMapId> {

    DiskLunMap getDiskIdByLunId(String lunId);

}
