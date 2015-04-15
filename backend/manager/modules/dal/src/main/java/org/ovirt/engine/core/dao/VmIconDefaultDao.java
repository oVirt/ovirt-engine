package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public interface VmIconDefaultDao extends GenericDao<VmIconDefault, Guid> {

    List<VmIconDefault> getByLargeIconId(Guid largeIconId);

    void removeAll();
}
