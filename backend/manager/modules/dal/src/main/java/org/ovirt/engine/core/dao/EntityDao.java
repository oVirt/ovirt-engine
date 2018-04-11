package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

public interface EntityDao extends Dao {
    String getEntityNameByIdAndType(Guid objectId, VdcObjectType vdcObjectType);
}
