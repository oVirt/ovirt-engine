package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.compat.Guid;

public interface VmIconDefaultDao extends GenericDao<VmIconDefault, Guid> {

    List<VmIconDefault> getByLargeIconId(Guid largeIconId);

    VmIconDefault getByOperatingSystemId(int osId);

    void removeAll();
}
