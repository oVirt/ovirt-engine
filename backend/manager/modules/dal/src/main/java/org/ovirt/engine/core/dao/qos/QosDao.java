package org.ovirt.engine.core.dao.qos;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface QosDao<T extends QosBase> extends GenericDao<T, Guid> {

    public List<T> getAllForStoragePoolId(Guid storagePoolId);

    public List<T> getAll(Guid userID, boolean isFiltered);
}
